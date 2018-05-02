package DecisionMaker;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tables.State;
import Mastership.CPManMastership;
import Scaling.ControllerScaling;

import java.util.ArrayList;

import static Database.Configure.Configuration.*;

public class NetworkingScalingAlgorithm extends AbstractDecisionMaker implements DecisionMaker {
    public NetworkingScalingAlgorithm() {
        decisionMakerName = decisionMakerType.SCALING_NETWORK;
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

        // Balancing first
        runBalancingOnly(state);

        boolean scaleInFlag = false;
        boolean scaleOutFlag = false;

        ControllerBean targetControllerScaleIn = null;
        ControllerBean targetControllerScaleOut = null;

        CPManMastership mastership = new CPManMastership();
        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();

        double maxNetLoad = getMaxNetworkLoad(state, activeControllers) * 8;
        double avgNetLoad = getAvgNetworkLoad(state, activeControllers) * 8;
        maxNetLoad = maxNetLoad / 1000000;
        avgNetLoad = avgNetLoad / 1000000;

        double upperThreshold = MAX_NET_BANDWIDTH * SCALING_THRESHOLD_UPPER;
        double lowerThreshold = MAX_NET_BANDWIDTH * SCALING_THRESHOLD_LOWER;
    }

    public double getTotalNetworkLoad(State state, ArrayList<ControllerBean> activeControllers) {

        double result = 0.0;

        for (ControllerBean controller : activeControllers) {
            result += state.getComputingResourceTuples().get(controller.getBeanKey()).avgNet();
        }

        return result;
    }

    public double getAvgNetworkLoad(State state, ArrayList<ControllerBean> activeControllers) {
        return getTotalNetworkLoad(state, activeControllers)/activeControllers.size();
    }

    public double getMaxNetworkLoad(State state, ArrayList<ControllerBean> activeControllers) {

        double result = 0.0;

        for (ControllerBean controller : activeControllers) {

            double tmpNetLoad = state.getComputingResourceTuples().get(controller.getBeanKey()).avgNet();
            if (tmpNetLoad > result) {

                result = tmpNetLoad;
            }
        }

        return result;
    }

    public void runCPManMastershipAlgorithm(State state) {
        CPManMastership mastership = new CPManMastership();
        mastership.runMastershipAlgorithm(state);
    }

    public void runBalancingOnly(State state) {
        runCPManMastershipAlgorithm(state);
    }

    public void runScaleIn(ControllerBean targetController, State state) {
        CPManMastership mastership = new CPManMastership();
        ControllerScaling scaling = new ControllerScaling();

        if (mastership.getActiveControllers().size() == 3) {
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

    public void runScaleOut(ControllerBean targetController, State state) {
        CPManMastership mastership = new CPManMastership();
        ControllerScaling scaling = new ControllerScaling();

        // Maximum number of controllers
        if (mastership.getActiveControllers().size() == Configuration.getInstance().getControllers().size()) {
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


}
