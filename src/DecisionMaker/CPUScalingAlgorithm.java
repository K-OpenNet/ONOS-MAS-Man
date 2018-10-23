package DecisionMaker;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tables.State;
import Mastership.CPManMastership;
import Scaling.ControllerScaling;

import java.util.ArrayList;
import java.util.HashMap;

import static Database.Configure.Configuration.*;

public class CPUScalingAlgorithm extends AbstractDecisionMaker implements DecisionMaker {

    public CPUScalingAlgorithm() {
        decisionMakerName = decisionMakerType.SCALING_CPU;
    }

    @Override
    public void runDecisionMakerAlgorithm(int currentTimeIndex, ArrayList<State> dbDump) {

        if (currentTimeIndex == 0 ) {
            return;
        }

        ArrayList<State> targetStates = new ArrayList<>();

        int startPoint = currentTimeIndex - Configuration.NOSCALING_CPMAN_PERIOD + 1;
        int endPoint = currentTimeIndex;

        for (int index = startPoint; index <= endPoint; index++) {
            targetStates.add(dbDump.get(index));
        }

        State state = mergeStates(targetStates);

        boolean scaleInFlag = false;
        boolean scaleOutFlag = false;

        ControllerBean targetControllerScaleIn = null;
        ControllerBean targetControllerScaleOut = null;

        CPManMastership mastership = new CPManMastership();
        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();
        int numActiveControllers = activeControllers.size();
        double averageCPULoad = averageCPULoadAllControllers(state);

        System.out.println("Scaling -- average CPU load: " + averageCPULoad);


        // check scaling out first
        for (ControllerBean controller : activeControllers) {
            double tmpCPULoad = state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage();
            double cpuNormalizingFactor = 40 / controller.getNumCPUs();
            tmpCPULoad = tmpCPULoad * cpuNormalizingFactor;

            if (tmpCPULoad > 100.0) {
                tmpCPULoad = 100;
            }

            //debugging code
            System.out.println("Scale-Out: " + controller.getControllerId() + " / " + tmpCPULoad);

            if (averageCPULoad > SCALING_THRESHOLD_UPPER && tmpCPULoad > SCALING_THRESHOLD_UPPER) {

                try {
                    targetControllerScaleOut = getTargetControllerForScaleOut();
                } catch (WrongScalingNumberControllers e) {
                    scaleOutFlag = false;
                    break;
                }
                scaleOutFlag = true;
                break;
            }
        }

        // check scaling in next
        int numTargetScaleInControllers = 0;
        for (ControllerBean controller : activeControllers) {
            double tmpCPULoad = state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage();
            double cpuNormalizingFactor = 40 / controller.getNumCPUs();
            tmpCPULoad = tmpCPULoad * cpuNormalizingFactor;

            if (tmpCPULoad > 100.0) {
                tmpCPULoad = 100;
            }

            //debugging code
            System.out.println("Scale-In: " + controller.getControllerId() + " / " + tmpCPULoad);

            if (controller.getControllerId().equals(FIXED_CONTROLLER_ID_1) ||
                    controller.getControllerId().equals(FIXED_CONTROLLER_ID_2) ||
                    controller.getControllerId().equals(FIXED_CONTROLLER_ID_3)) {
                continue;
            }

            if (averageCPULoad < SCALING_THRESHOLD_LOWER && tmpCPULoad < SCALING_THRESHOLD_LOWER) {
                targetControllerScaleIn = controller;
                numTargetScaleInControllers++;
                //scaleInFlag = true;
                //break;
            }
        }

        if (numTargetScaleInControllers > 1) {
            scaleInFlag = true;
        }



        if ((scaleOutFlag == true) && (numActiveControllers == Configuration.getInstance().getControllers().size())) {
            scaleInFlag = false;
            scaleOutFlag = false;
        } else if ((scaleInFlag == true) && (numActiveControllers == MIN_NUM_CONTROLLERS)) {
            scaleInFlag = false;
            scaleOutFlag = false;
        }

        //debugging code
        System.out.println("ScaleIn: " + scaleInFlag);
        System.out.println("Scaleout: " + scaleOutFlag);

        if (scaleOutFlag) {
            // decision making: does it need to scale out?
            System.out.println("Scale-Out: " + targetControllerScaleOut.getControllerId());// + " / " + state.getComputingResourceTuples().get(targetControllerScaleOut.getBeanKey()).avgNet());
            runScaleOut(targetControllerScaleOut, state);
        } else if (scaleInFlag) {
            // decision making: does it need to scale in?
            System.out.println("Scale-In: " + targetControllerScaleIn.getControllerId());// + " / " + state.getComputingResourceTuples().get(targetControllerScaleIn.getBeanKey()).avgNet());
            LAST_SCALEIN_CONTROLLER = targetControllerScaleIn.getControllerId();
            runScaleIn(targetControllerScaleIn, state);
        } else {
            // decision making: does it need to balance switch, only?
            System.out.println("Balancing only");
            runBalancingOnly(state);
        }

    }

    public void runCPManMastershipAlgorithm(State state) {
        CPManMastership mastership = new CPManMastership();
        mastership.runMastershipAlgorithm(state);
    }
/*
    public void runCPULoadMastershipAlgorithm(State state) {
        HashMap<String, ArrayList<String>> topology = new HashMap<>();
        HashMap<String, ArrayList<String>> dpidsOverSubControllers = new HashMap<>();

        CPManMastership mastership = new CPManMastership();
        double averageCPULoad = averageCPULoadAllControllers(state);
        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();
        HashMap<String, Double> oversubControllers = new HashMap<>();
        HashMap<String, Double> undersubControllers = new HashMap<>();

        // Key: Controller Id, Value: CPU usage / switch
        HashMap<String, Double> estimatedSwitchCPULoad = new HashMap<>();
        for (ControllerBean controller : activeControllers) {
            double tmpCPULoad = state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage();
            double cpuNormalizeFactor = 40/controller.getNumCPUs();
            tmpCPULoad = tmpCPULoad * cpuNormalizeFactor;
            int numSwitches = state.getMastershipTuples().get(controller.getBeanKey()).getSwitchList().size();

            if (tmpCPULoad > 100.0) {
                tmpCPULoad = 100;
            }

            double tmpCPULoadEachSwitch = 0;
            if (numSwitches != 0) {
                tmpCPULoadEachSwitch = tmpCPULoad / numSwitches;
            }


            if (tmpCPULoad < averageCPULoad) {
                undersubControllers.put(controller.getBeanKey(), tmpCPULoad);
            } else if (tmpCPULoad > averageCPULoad) {
                oversubControllers.put(controller.getBeanKey(), tmpCPULoad);
            }

            estimatedSwitchCPULoad.put(controller.getBeanKey(), tmpCPULoadEachSwitch);
        }

        // initialize topology
        for (String controllerId : undersubControllers.keySet()) {
            topology.put(controllerId, new ArrayList<>());
        }

        // cloning mastership monitoring result
        for (String controllerId : oversubControllers.keySet()) {
            ArrayList<String> dpids = new ArrayList<>();
            for (String dpid : state.getMastershipTuples().get(controllerId).getSwitchList()) {
                dpids.add(dpid);
            }
            dpidsOverSubControllers.put(controllerId, dpids);
        }

        //debugging code
//        System.out.println("Under subscriber controllers");
//        for (String underControllerId : undersubControllers.keySet()) {
//            System.out.println(underControllerId + ": " + undersubControllers.get(underControllerId));
//        }

        // make topology
        while (oversubControllers.size() != 0) {
            String highestOverSubController = getHighestCPULoadController(oversubControllers);
            int numSwitches = state.getMastershipTuples().get(highestOverSubController).getSwitchList().size();
            double tmpCPULoadSwitch = estimatedSwitchCPULoad.get(highestOverSubController);

            //debugging code
//            System.out.println("Over subscriber controllers");
//            System.out.println(highestOverSubController + ": " + oversubControllers.get(highestOverSubController) + " / " + numSwitches + " = " + tmpCPULoadSwitch);

            for (String undersubControllerId : undersubControllers.keySet()) {
                double undersubControllerLoad = undersubControllers.get(undersubControllerId);
                double oversubControllerLoad = oversubControllers.get(highestOverSubController);

                if (numSwitches < 1) {
                    break;
                } else if (oversubControllerLoad <= averageCPULoad) {
                    break;
                } else if (undersubControllerLoad + tmpCPULoadSwitch > averageCPULoad) {
                    continue;
                } else if (oversubControllerLoad - tmpCPULoadSwitch < averageCPULoad) {
                    break;
                }

                int maxNumSwitches = dpidsOverSubControllers.get(highestOverSubController).size();
                ArrayList<String> changedSwitches = new ArrayList<>();
                for (int index1 = 0; index1 < maxNumSwitches; index1++) {
                    String dpid = dpidsOverSubControllers.get(highestOverSubController).get(index1);

                    if (undersubControllerLoad + tmpCPULoadSwitch <= averageCPULoad &&
                            oversubControllerLoad - tmpCPULoadSwitch >= averageCPULoad &&
                            dpidsOverSubControllers.get(highestOverSubController).size() - changedSwitches.size() > 0) {
                        oversubControllerLoad -= tmpCPULoadSwitch;
                        undersubControllerLoad += tmpCPULoadSwitch;
                        //dpidsOverSubControllers.get(highestOverSubController).remove(dpid);
                        changedSwitches.add(dpid);
                        topology.get(undersubControllerId).add(dpid);
                        oversubControllers.replace(highestOverSubController, oversubControllerLoad);
                        undersubControllers.replace(undersubControllerId, undersubControllerLoad);

                        // debugging code
//                        System.out.println("Move switches");
//                        System.out.println(dpid + ": from " + highestOverSubController + " -> " + undersubControllerId);
//                        System.out.println("oversubLoad: " + oversubControllerLoad + " undersubLoad: " + undersubControllerLoad);
//                        System.out.println("numSwitches: " + dpidsOverSubControllers.get(highestOverSubController).size());

                    }
                }

                for (String dpid : changedSwitches) {
                    dpidsOverSubControllers.get(highestOverSubController).remove(dpid);
                }
            }

            //debugging code
//            System.out.println("Over subscriber controllers");
//            System.out.println(highestOverSubController + ": " + oversubControllers.get(highestOverSubController) + " / " + numSwitches + " = " + tmpCPULoadSwitch);

            oversubControllers.remove(highestOverSubController);
        }

        // debugging code
//        System.out.println("Topology");
//        for (String controllerId : topology.keySet()) {
//            System.out.print(controllerId + ": ");
//            for (String dpid : topology.get(controllerId)) {
//                System.out.print(dpid + " ");
//            }
//            System.out.println();
//        }

        mastership.changeMultipleMastership(topology);
    }
*/
    public double getMaxCPULoadSwitch (HashMap<String, Double> tmp) {

        double result = 0;

        if (tmp == null || tmp.size() == 0 ) {
            return result;
        }

        for (double tmpResult : tmp.values()) {
            if (result < tmpResult) {
                result = tmpResult;
            }
        }

        return result;

    }

    public double getMinCPULoadSwitch (HashMap<String, Double> tmp) {

        if (tmp == null || tmp.size() == 0 ) {
            return 0;
        }

        double result = tmp.values().iterator().next();

        for (double tmpResult : tmp.values()) {
            if (result > tmpResult) {
                result = tmpResult;
            }
        }

        return result;

    }

    public void runCPULoadMastershipAlgorithm(State state) {
        HashMap<String, ArrayList<String>> topology = new HashMap<>();
        HashMap<String, ArrayList<String>> dpidsOverSubControllers = new HashMap<>();

        CPManMastership mastership = new CPManMastership();
        double averageCPULoad = averageCPULoadAllControllers(state);
        HashMap<String, Double> oversubControllers = new HashMap<>();
        HashMap<String, Double> undersubControllers = new HashMap<>();

        // Key: Controller Id, Inner key: dpid, value: cpuload/net portion.
        HashMap<String, HashMap<String, Double>> estimatedSwitchCPULoad = new HashMap<>();
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            if (!controller.isActive()) continue;

            double tmpCPULoad = state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage();
            double cpuNormalizeFactor = 40 / controller.getNumCPUs();
            tmpCPULoad = tmpCPULoad * cpuNormalizeFactor;
            ArrayList<String> dpids = mastership.getSortedSwitchList(Configuration.getInstance().getControllerBeanWithId(controller.getControllerId()), state);

            if (tmpCPULoad > 100.0) {
                tmpCPULoad = 100;
            }

            System.out.println("********** Comp CPU load" + controller.getBeanKey() + ": cpuLoad - " + tmpCPULoad + " / avg cpuLoad: " + averageCPULoad);

            if (tmpCPULoad < averageCPULoad) {
                undersubControllers.put(controller.getBeanKey(), tmpCPULoad);
            } else if (tmpCPULoad > averageCPULoad) {
                oversubControllers.put(controller.getBeanKey(), tmpCPULoad);
            }

            if (dpids.size() == 0) {
                continue;
            }

            double tmpTotalControllerOFMsgs = (double) mastership.getNumOFMsgsForSingleController(Configuration.getInstance().getControllerBeanWithId(controller.getControllerId()), state);
            HashMap<String, Double> estimatedSwitchCPULoadEachController = new HashMap<>();

            for (String dpid: dpids) {
                double tmpSwitchOFMsgs = (double) mastership.getNumOFMsgsForSingleSwitchInMasterController(Configuration.getInstance().getControllerBeanWithId(controller.getControllerId()), dpid, state);
                double tmpFraction = tmpSwitchOFMsgs / tmpTotalControllerOFMsgs;
                double tmpCPULoadEachSwitch = tmpCPULoad * tmpFraction;

                estimatedSwitchCPULoadEachController.put(dpid, tmpCPULoadEachSwitch);
            }

            estimatedSwitchCPULoad.put(controller.getBeanKey(), estimatedSwitchCPULoadEachController);
        }

        // initialize topology
        for (String controllerId : undersubControllers.keySet()) {
            topology.put(controllerId, new ArrayList<>());
        }

        for (String controllerId : oversubControllers.keySet()) {
            ArrayList<String> dpids = new ArrayList<>();
            dpids = mastership.getSortedSwitchList(Configuration.getInstance().getControllerBeanWithId(controllerId), state);
            dpidsOverSubControllers.put(controllerId, dpids);
        }

        //debugging code
        System.out.println("Active  controllers");
        for (ControllerBean tmpControllerBean : Configuration.getInstance().getControllers()) {
            System.out.println("****************" + tmpControllerBean.getControllerId() + ": " + tmpControllerBean.isActive() + " / " + tmpControllerBean.isOnosAlive() + " / " + tmpControllerBean.isVmAlive());
        }
        System.out.println("Under subscriber controllers");
        for (String underControllerId : undersubControllers.keySet()) {
            System.out.println(underControllerId + ": " + undersubControllers.get(underControllerId));
        }
        System.out.println("Over subscriber controllers");
        for (String overControllerId : oversubControllers.keySet()) {
            System.out.println(oversubControllers + ": " + oversubControllers.get(overControllerId));
        }

        // make topology
        while (oversubControllers.size() != 0) {
            String highestOverSubController = getHighestCPULoadController(oversubControllers);
            int numSwitches = state.getMastershipTuples().get(highestOverSubController).getSwitchList().size();
            HashMap<String, Double> estimatedSwitchCPULoadEachController = estimatedSwitchCPULoad.get(highestOverSubController);

            //double tmpMaxCPULoadSwitch = getMaxCPULoadSwitch(estimatedSwitchCPULoadEachController);
            double tmpMinCPULoadSwitch = getMinCPULoadSwitch(estimatedSwitchCPULoadEachController);

            //debugging code
            //System.out.println("Over subscriber controllers");
            //System.out.println(highestOverSubController + ": " + oversubControllers.get(highestOverSubController) + " / " + numSwitches + " = " + tmpCPULoadSwitch);

            for (String undersubControllerId : undersubControllers.keySet()) {
                double undersubControllerLoad = undersubControllers.get(undersubControllerId);
                double oversubControllerLoad = oversubControllers.get(highestOverSubController);

                if (numSwitches < 1) {
                    break;
                } else if (oversubControllerLoad <= averageCPULoad) {
                    break;
                } else if (undersubControllerLoad + tmpMinCPULoadSwitch > averageCPULoad) {
                    continue;
                } else if (oversubControllerLoad - tmpMinCPULoadSwitch < averageCPULoad) {
                    break;
                }

                int maxNumSwitches = dpidsOverSubControllers.get(highestOverSubController).size();
                ArrayList<String> changedSwitches = new ArrayList<>();
                for (int index1 = 0; index1 < maxNumSwitches; index1++) {
                    String dpid = dpidsOverSubControllers.get(highestOverSubController).get(index1);

                    double tmpCPULoadSwitch = estimatedSwitchCPULoad.get(highestOverSubController).get(dpid);

                    if (undersubControllerLoad + tmpCPULoadSwitch <= averageCPULoad &&
                            oversubControllerLoad - tmpCPULoadSwitch >= averageCPULoad &&
                            dpidsOverSubControllers.get(highestOverSubController).size() - changedSwitches.size() > 0) {
                        oversubControllerLoad -= tmpCPULoadSwitch;
                        undersubControllerLoad += tmpCPULoadSwitch;
                        //dpidsOverSubControllers.get(highestOverSubController).remove(dpid);
                        changedSwitches.add(dpid);
                        topology.get(undersubControllerId).add(dpid);
                        oversubControllers.replace(highestOverSubController, oversubControllerLoad);
                        undersubControllers.replace(undersubControllerId, undersubControllerLoad);

                        // debugging code
                        System.out.println("Move switches");
                        System.out.println(dpid + ": from " + highestOverSubController + " -> " + undersubControllerId);
                        System.out.println("oversubLoad: " + oversubControllerLoad + " undersubLoad: " + undersubControllerLoad);
                        System.out.println("numSwitches: " + dpidsOverSubControllers.get(highestOverSubController).size());

                    }
                }

                for (String dpid : changedSwitches) {
                    dpidsOverSubControllers.get(highestOverSubController).remove(dpid);
                }
            }

            //debugging code
            //System.out.println("Over subscriber controllers");
            //System.out.println(highestOverSubController + ": " + oversubControllers.get(highestOverSubController) + " / " + numSwitches + " = " + tmpCPULoadSwitch);

            oversubControllers.remove(highestOverSubController);
        }

        // debugging code
        System.out.println("Topology");
        for (String controllerId : topology.keySet()) {
            System.out.print(controllerId + ": ");
            for (String dpid : topology.get(controllerId)) {
                System.out.print(dpid + " ");
            }
            System.out.println();
        }

        mastership.changeMultipleMastership(topology);
    }

    public String getHighestCPULoadController(HashMap<String, Double> rawResult) {

        String result = null;
        double highestCPULoad = 0;

        for (String controllerId :rawResult.keySet()) {

            if (result == null) {
                result = controllerId;
                highestCPULoad = rawResult.get(controllerId);
            } else if (highestCPULoad < rawResult.get(controllerId)) {
                result = controllerId;
                highestCPULoad = rawResult.get(controllerId);
            }
        }

        return result;
    }

    public double averageCPULoadAllControllers(State state) {
        double result = 0.0;

        CPManMastership mastership = new CPManMastership();
        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();
        int numActiveControllers = activeControllers.size();

        for (ControllerBean controller : activeControllers) {
            double tmpCPULoad = state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage();
            double cpuNormalizeFactor = 40/controller.getNumCPUs();
            tmpCPULoad = tmpCPULoad * cpuNormalizeFactor;

            if (tmpCPULoad > 100.0) {
                tmpCPULoad = 100;
            }

            result += tmpCPULoad;
        }

        return result / numActiveControllers;
    }

    public ControllerBean getTargetControllerForScaleOut() {

        ControllerBean lastController = null;

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {

            if (controller.getControllerId().equals(Configuration.LAST_SCALEIN_CONTROLLER)) {
                if (controller.isActive() == false) {
                    lastController = controller;
                }
                continue;
            }

            if (controller.isActive() == false) {
                return controller;
            }
        }

        if (lastController == null) {
            throw new WrongScalingNumberControllers();
        }

        return lastController;
    }

    public void runScaleOut(ControllerBean targetController, State state) {
        CPManMastership mastership = new CPManMastership();
        ControllerScaling scaling = new ControllerScaling();

        // Maximum number of controllers
        if (mastership.getActiveControllers().size() == Configuration.getInstance().getControllers().size()) {
            //runCPManMastershipAlgorithm(state); // change mastership --> considering CPU load
            runCPULoadMastershipAlgorithm(state);
            return;
        }


        switch(SCALING_LEVEL) {
            case 1:
                scaling.runL1ONOSScaleOut(targetController, state);
                break;
            case 2:
                scaling.runL2ONOSScaleOut(targetController, state);
                break;
            case 3:
                scaling.runL3ONOSScaleOut(targetController, state);
                break;
            default:
                throw new WrongScalingLevelException();
        }

    }

    public void runBalancingOnly(State state) {
        //runCPManMastershipAlgorithm(state); // change mastership --> considering CPU load
        runCPULoadMastershipAlgorithm(state);
    }

    public void runScaleIn(ControllerBean targetController, State state) {
        CPManMastership mastership = new CPManMastership();
        ControllerScaling scaling = new ControllerScaling();

        if (mastership.getActiveControllers().size() == 3) {
            //runCPManMastershipAlgorithm(state); // change mastership --> considering CPU load
            runCPULoadMastershipAlgorithm(state);
            return;
        }

        switch(SCALING_LEVEL) {
            case 1:
                scaling.runL1ONOSScaleIn(targetController, state);
                break;
            case 2:
                scaling.runL2ONOSScaleIn(targetController, state);
                break;
            case 3:
                scaling.runL3ONOSScaleIn(targetController, state);
                break;
            default:
                throw new WrongScalingLevelException();
        }
    }

}

class WrongScalingLevelException extends RuntimeException {
    public WrongScalingLevelException() {
    }

    public WrongScalingLevelException(String message) {
        super(message);
    }
}

class WrongScalingNumberControllers extends RuntimeException {
    public WrongScalingNumberControllers() {
    }

    public WrongScalingNumberControllers(String message) {
        super(message);
    }
}
