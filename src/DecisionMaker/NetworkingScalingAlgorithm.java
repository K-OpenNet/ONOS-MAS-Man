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

        CPManMastership mastership = new CPManMastership();
        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();

        double maxNetLoad = getMaxNetworkLoad(state, activeControllers) * 8;
        double avgNetLoad = getAvgNetworkLoad(state, activeControllers) * 8;
        maxNetLoad = maxNetLoad / 1000000;
        avgNetLoad = avgNetLoad / 1000000;

        double upperThreshold = (double) MAX_NET_BANDWIDTH * ((double) SCALING_THRESHOLD_UPPER/100);
        double lowerThreshold = (double) MAX_NET_BANDWIDTH * ((double) SCALING_THRESHOLD_LOWER/100);

        System.out.println("MAX Load: " + maxNetLoad + " / Avg Load: " + avgNetLoad);
        System.out.println("Up Threshold: " + upperThreshold + " / Bottom Threshold: " + lowerThreshold);

        if (maxNetLoad > upperThreshold && activeControllers.size() < Configuration.getInstance().getControllers().size()) {

            ControllerBean targetControllerScaleOut = getTargetControllerForScaleOut();
            System.out.println("Scale-Out: " + targetControllerScaleOut.getControllerId() + " / " + state.getComputingResourceTuples().get(targetControllerScaleOut.getBeanKey()).avgNet());
            runScaleOut(targetControllerScaleOut, state);

        } else if (avgNetLoad < lowerThreshold && activeControllers.size() > MIN_NUM_CONTROLLERS) {

            ControllerBean targetControllerScaleIn = getTargetControllerForScaleIn(state, activeControllers);
            System.out.println("Scale-In: " + targetControllerScaleIn.getControllerId() + " / " + state.getComputingResourceTuples().get(targetControllerScaleIn.getBeanKey()).avgNet());
            runScaleIn(targetControllerScaleIn, state);
        } else {
            System.out.println("Balancing only");
            runBalancingOnly(state);
        }
    }

    public ControllerBean getTargetControllerForScaleIn(State state, ArrayList<ControllerBean> activeControllers) {

        ControllerBean result = null;
        double lowestNetLoad = Double.MAX_VALUE;

        for (ControllerBean controller : activeControllers) {

            if (controller.getControllerId().equals(FIXED_CONTROLLER_ID_1) ||
                    controller.getControllerId().equals(FIXED_CONTROLLER_ID_2) ||
                    controller.getControllerId().equals(FIXED_CONTROLLER_ID_3)) {
                continue;
            }

            double tmpNetLoad = state.getComputingResourceTuples().get(controller.getBeanKey()).avgNet();

            // debugging code
            System.out.println("ControllerId: " + controller.getControllerId() + " / net load: " + tmpNetLoad + " / lowestLoad: " + lowestNetLoad);

            if (lowestNetLoad > tmpNetLoad) {
                result = controller;
                lowestNetLoad = tmpNetLoad;
            }
        }

        LAST_SCALEIN_CONTROLLER = result.getControllerId();
        return result;
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
