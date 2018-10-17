package DecisionMaker;

import Beans.ControllerBean;
import Controller.Controller;
import Database.Configure.Configuration;
import Database.Tables.State;
import Mastership.CPManMastership;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
        System.out.println("Start L1 algorithm");
    }

    public void runLane2Algorithm(State state) {
        System.out.println("Start L2 algorithm");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("End L2 algorithm");
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