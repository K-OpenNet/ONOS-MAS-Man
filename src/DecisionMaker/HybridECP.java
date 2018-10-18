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

        System.out.println("*** End L1 algorithm");
    }

    public void runLane2Algorithm(State state) {
        System.out.println("*** Start L2 algorithm");
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

        System.out.println("*** End L2 algorithm");
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

        Date dt = new Date();
        long startTime = dt.getTime();
        ecp.runLane1Algorithm(state);
        dt = new Date();
        System.out.println("** L1 Scaling time: " + (dt.getTime() - startTime) + " (timeslot: " + startTimeIndex + ", Algorithm: " + "HECP" + ")");

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

        System.out.println();
        Date dt = new Date();
        long startTime = dt.getTime();
        ecp.runLane2Algorithm(state);
        dt = new Date();
        System.out.println("** L2 Scaling time: " + (dt.getTime() - startTime) + " (timeslot: " + startTimeIndex + ", Algorithm: " + "HECP" + ")");

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
        scaling.switchOffControllerForScaleIn(targetController, state);
        targetController.setOnosAlive(false);
        scaling.switchOffVMForScaleIn(targetController, state);
        targetController.setVmAlive(false);
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