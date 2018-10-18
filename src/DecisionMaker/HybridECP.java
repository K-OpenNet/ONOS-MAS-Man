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

        if (currentTimeIndex == 0) {
            return;
        }

        ArrayList<State> targetStates = new ArrayList<>();

        int startPoint = currentTimeIndex - Configuration.NOSCALING_CPMAN_PERIOD + 1;
        int endPoint = currentTimeIndex;

        for (int index = startPoint; index <= endPoint; index++) {
            targetStates.add(dbDump.get(index));
        }

        State state = mergeStates(targetStates);

        ThreadLane1Algorithm runnableThreadLane1Algorithm = new ThreadLane1Algorithm(this, state, currentTimeIndex);
        ThreadLane2Algorithm runnableThreadLane2Algorithm = new ThreadLane2Algorithm(this, state, currentTimeIndex);
        Thread threadL1 = new Thread(runnableThreadLane1Algorithm);
        Thread threadL2 = new Thread(runnableThreadLane2Algorithm);
        threadL1.start();
        threadL2.start();
        try {
            threadL1.join(); //not for L2 --> independent algorithm which has an independent lock
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void runLane1Algorithm(State state) {
        System.out.println("*** Start L1 algorithm");

        Date dt = new Date();
        long startTime = dt.getTime();

        ElastiConAwareL1Algorithm(state);
        //EASAwareL1Algorithm(state);

        dt = new Date();
        System.out.println("** L1 Scaling time: " + (dt.getTime() - startTime) + " (timeslot: " + Controller.getTimeIndex() + ", Algorithm: " + "HECP" + ")");

        System.out.println("*** End L1 algorithm");
    }

    public void ElastiConAwareL1Algorithm (State state) {
        boolean scaleInFlag = false;
        boolean scaleOutFlag = false;
        ControllerBean targetControllerScaleIn = null;
        ControllerBean targetControllerScaleOut = null;
        CPManMastership mastership = new CPManMastership();
        ArrayList<ControllerBean> activeControllers = getActiveControllers();
        int numActiveControllers = getNumActiveControllers();
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

        System.out.println("ScaleIn: " + scaleInFlag);
        System.out.println("Scaleout: " + scaleOutFlag);

        ControllerScaling scaling = new ControllerScaling();

        if (scaleOutFlag) {
            System.out.println("Scale-Out: " + targetControllerScaleOut.getControllerId());// + " / " + state.getComputingResourceTuples().get(targetControllerScaleOut.getBeanKey()).avgNet());
            if (numActiveControllers == Configuration.getInstance().getControllers().size()) {
                runCPULoadMastershipAlgorithm(state);
            }
            targetControllerScaleOut.setActive(true);
            scaling.distributeMastershipForScaleOutElastiCon(targetControllerScaleOut, state);
        } else if (scaleInFlag) {
            System.out.println("Scale-In: " + targetControllerScaleIn.getControllerId());// + " / " + state.getComputingResourceTuples().get(targetControllerScaleIn.getBeanKey()).avgNet());
            LAST_SCALEIN_CONTROLLER = targetControllerScaleIn.getControllerId();
            if (numActiveControllers == 3) {
                runCPULoadMastershipAlgorithm(state);
            }
            scaling.distributeMastershipForScaleInElastiCon(targetControllerScaleIn, state);
            targetControllerScaleIn.setActive(false);
        } else {
            System.out.println("Balancing only");
            runCPULoadMastershipAlgorithm(state);
        }

    }

    public void EASAwareL1Algorithm (State state) {

    }

    public void runLane2Algorithm(State state) {
        System.out.println("*** Start L2 algorithm");
        Date dt = new Date();
        long startTime = dt.getTime();

        int currentNumStandbyControllers = getNumStandbyControllers();
        int diffNumStandbyControllers = Configuration.getInstance().NUM_STANDBY_CONTROLLER - currentNumStandbyControllers;
        //int diffNumStandbyControllers = 2 - currentNumStandbyControllers;
        if (diffNumStandbyControllers > 0) {
            System.out.println("*** L2: Need to switch on " + diffNumStandbyControllers + " controllers");
            switchOnMultipleControllers(diffNumStandbyControllers, state);
        } else if (diffNumStandbyControllers < 0) {
            System.out.println("*** L2: Need to switch off " + Math.abs(diffNumStandbyControllers) + " controllers");
            switchOffMultipleControllers(Math.abs(diffNumStandbyControllers), state);
        } else {
            System.out.println("*** L2: No need to power on/off controllers");
        }

        dt = new Date();
        System.out.println("** L2 Scaling time: " + (dt.getTime() - startTime) + " (timeslot: " + Controller.getTimeIndex() + ", Algorithm: " + "HECP" + ")");
        System.out.println("*** End L2 algorithm");
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

    public void switchOnMultipleControllers(int numTargetControllers, State state) {
        ArrayList<ControllerBean> targetControllersSwitchOn = getTargetControllerSwitchOn(numTargetControllers);

        ArrayList<Runnable> runnables = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();
        for (ControllerBean controller : targetControllersSwitchOn) {
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

        /* debugging code
        System.out.println("*!*!* WKIM: switched on controllers");
        for (ControllerBean controller : targetControllersSwitchOn) {
            System.out.println(controller.getControllerId());
        }
        System.out.println("*!*!* WKIM: total controllers");
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            System.out.println(controller.getControllerId() + ": " + controller.isActive() + "/" + controller.isOnosAlive() + "/" + controller.isVmAlive());
        }
        */

    }

    public void switchOffMultipleControllers(int numTargetControllers, State state) {
        ArrayList<ControllerBean> targetControllersSwitchOff = getTargetControllerSwitchOff(numTargetControllers);

        ArrayList<Runnable> runnables = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();
        for (ControllerBean controller : targetControllersSwitchOff) {
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

        /* debugging code
        System.out.println("*!*!* WKIM: switched off controllers");
        for (ControllerBean controller : targetControllersSwitchOff) {
            System.out.println(controller.getControllerId());
        }
        System.out.println("*!*!* WKIM: total controllers");
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            System.out.println(controller.getControllerId() + ": " + controller.isActive() + "/" + controller.isOnosAlive() + "/" + controller.isVmAlive());
        }
        */
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


    public void runCPULoadMastershipAlgorithm(State state) {
        CPUScalingAlgorithm scaling = new CPUScalingAlgorithm();
        scaling.runBalancingOnly(state);
    }

    public ArrayList<ControllerBean> getActiveControllers() {
        ArrayList<ControllerBean> activeControllers = new ArrayList<>();

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            if (controller.isActive()) {
                activeControllers.add(controller);
            }
        }

        return activeControllers;
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

    public int getNumStandbyControllers() {
        int numStandbyControllers = 0;

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            if (!controller.isActive() && controller.isOnosAlive() && controller.isVmAlive()) {
                numStandbyControllers++;
            }
        }

        return numStandbyControllers;
    }
}

class ThreadLane1Algorithm implements Runnable {

    HybridECP ecp;
    State state;
    int startTimeIndex;

    public ThreadLane1Algorithm(HybridECP ecp, State state, int startTimeIndex) {
        this.ecp = ecp;
        this.state = state;
        this.startTimeIndex = startTimeIndex;
    }

    @Override
    public void run() {
        if (Controller.hecpL1Lock.isLocked()) {
            System.out.println("*** L1Algorithm is not finished yet (timeslot: " + startTimeIndex + ")");
            return;
        }
        Controller.hecpL1Lock.lock();

        ecp.runLane1Algorithm(state);

        Controller.hecpL1Lock.unlock();
    }
}

class ThreadLane2Algorithm implements Runnable {

    HybridECP ecp;
    State state;
    int startTimeIndex;

    public ThreadLane2Algorithm(HybridECP ecp, State state, int startTimeIndex) {
        this.ecp = ecp;
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

        ecp.runLane2Algorithm(state);

        Controller.hecpL2Lock.unlock();
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
            doStandbySwitchOn();
        } else if (switchOnOff == SWITCH_ON_OFF.SWITCH_OFF) {
            doStandbySwitchOff();
        }
    }

    public void doStandbySwitchOn() {
        System.out.println("*** Start to switch on " + targetController.getControllerId());
        scaling.switchOnVMForScaleOut(targetController, state);
        targetController.setVmAlive(true);
        scaling.switchOnControllerForScaleOut(targetController, state);
        targetController.setOnosAlive(true);
        System.out.println("*** Finish to switch on " + targetController.getControllerId());
    }


    public void doStandbySwitchOff() {
        System.out.println("*** Start to switch off " + targetController.getControllerId());
        if (targetController.getControllerId().equals(Configuration.FIXED_CONTROLLER_ID_1) ||
                targetController.getControllerId().equals(Configuration.FIXED_CONTROLLER_ID_2) ||
                targetController.getControllerId().equals(Configuration.FIXED_CONTROLLER_ID_3)) {
            throw new SwitchOffException();
        }
        targetController.setOnosAlive(false);
        scaling.switchOffControllerForScaleIn(targetController, state);
        targetController.setVmAlive(false);
        scaling.switchOffVMForScaleIn(targetController, state);
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