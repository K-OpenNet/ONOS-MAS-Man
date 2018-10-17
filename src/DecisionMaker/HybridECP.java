package DecisionMaker;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tables.State;

import java.util.ArrayList;

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

    }

    public void runLane1Algorithm() {

    }

    public void runLane2Algorithm() {

    }

    public void runCPULoadMastershipAlgorithm(State state) {

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
