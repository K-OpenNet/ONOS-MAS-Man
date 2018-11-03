package DecisionMaker;

import Beans.ControllerBean;
import Controller.Controller;
import Database.Configure.Configuration;
import Database.Tables.State;
import Mastership.CPManMastership;
import Scaling.ControllerScaling;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static Database.Configure.Configuration.*;

enum SWITCH_ON_OFF {
    SWITCH_ON, SWITCH_OFF;
}

public class HybridECP extends AbstractDecisionMaker implements DecisionMaker {

    public HybridECP() {
        decisionMakerName = decisionMakerType.HECP;
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

        ThreadLane2Algorithm runnableLane2 = new ThreadLane2Algorithm(state, currentTimeIndex);
        Thread threadLane2 = new Thread(runnableLane2);
        threadLane2.start();

        System.out.println("*** Start L1 algorithm");

        Date dt = new Date();
        long startTime = dt.getTime();

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
            System.out.println("Scale-Out: " + targetControllerScaleOut.getControllerId());// + " / " + state.getComputingResourceTuples().get(targetControllerScaleOut.getBeanKey()).avgNet());
            runScaleOut(targetControllerScaleOut, state);
        } else if (scaleInFlag) {
            System.out.println("Scale-In: " + targetControllerScaleIn.getControllerId());// + " / " + state.getComputingResourceTuples().get(targetControllerScaleIn.getBeanKey()).avgNet());
            runScaleIn(targetControllerScaleIn, state);
        } else {
            System.out.println("!!!!!!!!!!! Current Timeindex: " + Controller.getTimeIndex() + ", Last scaleout time: " + LAST_SCALEOUT_TIME_INDEX + ", Last scalein time: " + LAST_SCALEIN_TIME_INDEX);
            if (Controller.getTimeIndex() < 2) {

            } else if (LAST_SCALEOUT_TIME_INDEX == -1 && LAST_SCALEIN_TIME_INDEX == -1) {
                System.out.println("Balancing only");
                runCPULoadMastershipAlgorithm(state);
            } else if (Controller.getTimeIndex() - LAST_SCALEOUT_TIME_INDEX > NUM_BUBBLE && Controller.getTimeIndex() - LAST_SCALEIN_TIME_INDEX > NUM_BUBBLE) {
                System.out.println("Balancing only");
                runCPULoadMastershipAlgorithm(state);
            } else {
                System.out.println("No need to rebalance!");
            }
        }

        dt = new Date();
        System.out.println("** L1 Scaling time: " + (dt.getTime() - startTime) + " (timeslot: " + Controller.getTimeIndex() + ", Algorithm: " + "HECP" + ")");

        System.out.println("*** End L1 algorithm");
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

        scaling.runL1ONOSScaleOut(targetController, state);
    }

    public void runScaleIn(ControllerBean targetController, State state) {
        CPManMastership mastership = new CPManMastership();
        ControllerScaling scaling = new ControllerScaling();

        if (mastership.getActiveControllers().size() == 3) {
            //runCPManMastershipAlgorithm(state); // change mastership --> considering CPU load
            runCPULoadMastershipAlgorithm(state);
            return;
        }

        scaling.runL1ONOSScaleIn(targetController, state);
    }

    public void runBalancingOnly(State state) {
        //runCPManMastershipAlgorithm(state); // change mastership --> considering CPU load
        runCPULoadMastershipAlgorithm(state);
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

    public ControllerBean getTargetControllerForScaleOut() {

        ControllerBean lastController = null;

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {

            if (controller.getControllerId().equals(Configuration.LAST_SCALEIN_CONTROLLER)) {
                if (controller.isActive() == false && controller.isVmAlive() && controller.isOnosAlive()) {
                    lastController = controller;
                }
                continue;
            }

            if (controller.isActive() == false && controller.isVmAlive() && controller.isOnosAlive()) {
                return controller;
            }
        }

        if (lastController == null) {
            throw new WrongScalingNumberControllers();
        }

        return lastController;
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
}

class ThreadLane2Algorithm implements Runnable {

    State state;
    int startTimeIndex;

    public ThreadLane2Algorithm(State state, int startTimeIndex) {
        this.state = state;
        this.startTimeIndex = startTimeIndex;
    }

    @Override
    public void run() {
        if (Controller.hecpL2Lock.isLocked()) {
            System.out.println("*** L2Algorithm is not finished yet (timeslot: " + startTimeIndex + ")");
            return;
        }

        Controller.hecpL2Lock.lock();
        runLane2Algorithm();
        Controller.hecpL2Lock.unlock();
    }

    public void runLane2Algorithm() {
        System.out.println("*** Start L2 algorithm");
        Date dt = new Date();
        long startTime = dt.getTime();

        int currentNumActiveControllers = getNumActiveControllers();
        int currentNumStandbyControllers = getNumStandbyControllers();
        int diffNumStandbyControllers = Configuration.getInstance().NUM_STANDBY_CONTROLLER - currentNumStandbyControllers;
        int currentNumInactiveControllers = Configuration.getInstance().getControllers().size() - currentNumActiveControllers - currentNumStandbyControllers;
        boolean switchOnFlag = false;
        boolean switchOffFlag = false;

        if (currentNumStandbyControllers < Configuration.getInstance().NUM_STANDBY_CONTROLLER) {

            if (currentNumInactiveControllers != 0 && currentNumInactiveControllers < diffNumStandbyControllers) {
                System.out.println("*** L2: Need to switch on " + diffNumStandbyControllers + " controllers -> " + currentNumInactiveControllers + " controllers");
                switchOnMultipleControllers(currentNumInactiveControllers, state);
                switchOnFlag = true;
            } else if (currentNumInactiveControllers != 0 && currentNumInactiveControllers >= diffNumStandbyControllers) {
                System.out.println("*** L2: Need to switch on " + diffNumStandbyControllers + " controllers");
                switchOnMultipleControllers(diffNumStandbyControllers, state);
                switchOnFlag = true;
            } else {
                System.out.println("*** L2: Cannot switch on " + diffNumStandbyControllers + " controllers");
            }

        } else if (currentNumStandbyControllers > Configuration.getInstance().NUM_STANDBY_CONTROLLER) {
            if (currentNumActiveControllers == 3) {
                System.out.println("*** L2: Cannot switch off " + diffNumStandbyControllers + " controllers");
            } else if (currentNumActiveControllers + diffNumStandbyControllers < 3) {
                System.out.println("*** L2: Need to switch off " + Math.abs(diffNumStandbyControllers) + " controllers -> " + (currentNumActiveControllers - 3) + " controllers");
                switchOffMultipleControllers((currentNumActiveControllers-3), state);
            } else {
                System.out.println("*** L2: Need to switch off " + Math.abs(diffNumStandbyControllers) + " controllers");
                switchOffMultipleControllers(Math.abs(diffNumStandbyControllers), state);
            }
        } else {
            System.out.println("*** L2: No need to power on/off controllers");
        }
/*
        if (diffNumStandbyControllers > 0) {
            System.out.println("*** L2: Need to switch on " + diffNumStandbyControllers + " controllers");
            switchOnMultipleControllers(diffNumStandbyControllers, state);
        } else if (diffNumStandbyControllers < 0) {
            System.out.println("*** L2: Need to switch off " + Math.abs(diffNumStandbyControllers) + " controllers");
            switchOffMultipleControllers(Math.abs(diffNumStandbyControllers), state);
        } else {
            System.out.println("*** L2: No need to power on/off controllers");
        }
*/
        dt = new Date();
        System.out.println("** L2 Scaling time: " + (dt.getTime() - startTime) + " (timeslot: " + Controller.getTimeIndex() + ", Algorithm: " + "HECP" + ")");
        System.out.println("*** End L2 algorithm");
        if (switchOnFlag) {
            LAST_SCALEOUT_TIME_INDEX = Controller.getTimeIndex();
        } else if (switchOffFlag) {
            LAST_SCALEIN_TIME_INDEX = Controller.getTimeIndex();
        }
    }

    public void switchOnMultipleControllers(int numTargetControllers, State state) {
        ArrayList<ControllerBean> targetControllersSwitchOn = getTargetControllerSwitchOn(numTargetControllers);

        ArrayList<Runnable> runnables = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();
        for (ControllerBean controller : targetControllersSwitchOn) {
            System.out.println("SWITCH-ON: " + controller.getControllerId());
            ThreadSwitchOnOff runnable = new ThreadSwitchOnOff(controller, SWITCH_ON_OFF.SWITCH_ON, state);
            Thread thread = new Thread(runnable);
            runnables.add(runnable);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void switchOffMultipleControllers(int numTargetControllers, State state) {
        ArrayList<ControllerBean> targetControllersSwitchOff = getTargetControllerSwitchOff(numTargetControllers);

        ArrayList<Runnable> runnables = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();
        for (ControllerBean controller : targetControllersSwitchOff) {
            System.out.println("SWITCH-OFF: " + controller.getControllerId());
            ThreadSwitchOnOff runnable = new ThreadSwitchOnOff(controller, SWITCH_ON_OFF.SWITCH_OFF, state);
            Thread thread = new Thread(runnable);
            runnables.add(runnable);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<ControllerBean> getTargetControllerSwitchOn (int numTargetControllers) {
        ArrayList<ControllerBean> targetControllers = new ArrayList<>();

        int maxNumber = Configuration.getInstance().getControllers().size();
        int numActiveControllers = getNumActiveControllers();
        int numStandbyControllers = getNumStandbyControllers();

        if (maxNumber <= numActiveControllers + numStandbyControllers + numTargetControllers) {
            for (ControllerBean controller : Configuration.getInstance().getControllers()) {
                if (!controller.isVmAlive()) {
                    targetControllers.add(controller);
                }
            }
        } else {
            int tmpNumTargetController = 0;
            for (ControllerBean controller : Configuration.getInstance().getControllers()) {
                if (!controller.isVmAlive()) {
                    targetControllers.add(controller);
                    tmpNumTargetController++;
                    if (tmpNumTargetController == numTargetControllers) {
                        break;
                    }
                }
            }
        }

        return targetControllers;
    }

    public ArrayList<ControllerBean> getTargetControllerSwitchOff (int numTargetControllers) {
        ArrayList<ControllerBean> targetControllers = new ArrayList<>();

        int tmpNumTargetController = 0;
        for (ControllerBean controller : getStandByControllers()) {
            targetControllers.add(controller);
            tmpNumTargetController++;
            if (tmpNumTargetController == numTargetControllers) {
                break;
            }
        }

        return targetControllers;
    }

    public int getNumStandbyControllers() {
        int numStandbyControllers = 0;

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            if (!controller.isActive() && controller.isOnosAlive() && controller.isVmAlive()) {
                numStandbyControllers++;
            }
        }

        return numStandbyControllers;
    }

    public int getNumActiveControllers() {
        int numActiveControllers = 0;

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            if (controller.isActive()) {
                numActiveControllers++;
            }
        }

        return numActiveControllers;
    }

    public ArrayList<ControllerBean> getStandByControllers() {
        ArrayList<ControllerBean> standbyControllers = new ArrayList<>();

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            if (!controller.isActive() && controller.isOnosAlive() && controller.isVmAlive()) {
                standbyControllers.add(controller);
            }
        }

        return standbyControllers;
    }
}

class ThreadSwitchOnOff implements Runnable {

    ControllerScaling scaling;
    ControllerBean targetController;
    SWITCH_ON_OFF switchOnOff;
    State state;

    public ThreadSwitchOnOff(ControllerBean targetController, SWITCH_ON_OFF switchOnOff, State state) {
        this.scaling = new ControllerScaling();
        this.targetController = targetController;
        this.switchOnOff = switchOnOff;
        this.state = state;
    }

    @Override
    public void run() {
        if (switchOnOff == SWITCH_ON_OFF.SWITCH_ON) {
            doSwitchOn();
        } else {
            doSwitchOff();
        }
    }

    public void doSwitchOn() {
        System.out.println("*** Start to switch on " + targetController.getControllerId());
        scaling.switchOnVMForScaleOut(targetController, state);
        targetController.setVmAlive(true);
        scaling.switchOnControllerForScaleOut(targetController, state);
        targetController.setOnosAlive(true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("*** Finish to switch on " + targetController.getControllerId());
    }

    public void doSwitchOff() {
        System.out.println("*** Start to switch off " + targetController.getControllerId());
        targetController.setOnosAlive(false);
        scaling.switchOffControllerForScaleIn(targetController, state);
        targetController.setVmAlive(false);
        scaling.switchOffVMForScaleIn(targetController, state);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("*** Finish to switch off " + targetController.getControllerId());
    }
}


class SwitchOffException extends RuntimeException {
    public SwitchOffException() {
    }

    public SwitchOffException(String message) {
        super(message);
    }
}