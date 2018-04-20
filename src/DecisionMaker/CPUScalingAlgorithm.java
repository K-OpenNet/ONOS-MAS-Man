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
        for (ControllerBean controller : activeControllers) {
            double tmpCPULoad = state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage();
            double cpuNormalizingFactor = 40 / controller.getNumCPUs();
            tmpCPULoad = tmpCPULoad * cpuNormalizingFactor;

            //debugging code
            System.out.println("Scale-In: " + controller.getControllerId() + " / " + tmpCPULoad);

            if (controller.getControllerId().equals(FIXED_CONTROLLER_ID_1) ||
                    controller.getControllerId().equals(FIXED_CONTROLLER_ID_2) ||
                    controller.getControllerId().equals(FIXED_CONTROLLER_ID_3)) {
                continue;
            }

            if (averageCPULoad < SCALING_THRESHOLD_LOWER && tmpCPULoad < SCALING_THRESHOLD_LOWER) {
                targetControllerScaleIn = controller;
                scaleInFlag = true;
                break;
            }
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
            runScaleOut(targetControllerScaleOut, state);
        } else if (scaleInFlag) {
            // decision making: does it need to scale in?
            runScaleIn(targetControllerScaleIn, state);
        } else {
            // decision making: does it need to balance switch, only?
            runBalancingOnly(state);
        }

    }

    public void runCPManMastershipAlgorithm(State state) {
        CPManMastership mastership = new CPManMastership();
        mastership.runMastershipAlgorithm(state);
    }

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

        // make topology
        while (oversubControllers.size() == 0) {
            String highestOverSubController = getHighestCPULoadController(oversubControllers);
            int numSwitches = state.getMastershipTuples().get(highestOverSubController).getSwitchList().size();
            double tmpCPULoadSwitch = estimatedSwitchCPULoad.get(highestOverSubController);

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
                for (int index1 = 0; index1 < maxNumSwitches; index1++) {
                    String dpid = dpidsOverSubControllers.get(highestOverSubController).get(index1);

                    if (undersubControllerLoad + tmpCPULoadSwitch <= averageCPULoad &&
                            oversubControllerLoad - tmpCPULoadSwitch >= averageCPULoad &&
                            dpidsOverSubControllers.get(highestOverSubController).size() > 0) {
                        oversubControllerLoad -= tmpCPULoadSwitch;
                        undersubControllerLoad += tmpCPULoadSwitch;
                        dpidsOverSubControllers.get(highestOverSubController).remove(dpid);
                        topology.get(undersubControllerId).add(dpid);
                    } else {
                        oversubControllers.replace(highestOverSubController, oversubControllerLoad);
                        undersubControllers.replace(undersubControllerId, undersubControllerLoad);
                        break;
                    }
                }
            }
            oversubControllers.remove(highestOverSubController);
        }

        // debugging code
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
            result += tmpCPULoad;
        }

        return result / numActiveControllers;
    }

    public ControllerBean getTargetControllerForScaleOut() {

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            if (controller.isActive() == false) {
                return controller;
            }
        }

        throw new WrongScalingNumberControllers();
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
