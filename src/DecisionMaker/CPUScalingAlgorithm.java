package DecisionMaker;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tables.State;
import Mastership.CPManMastership;
import Scaling.ControllerScaling;

import java.util.ArrayList;

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
        double averageCPULoad = 0.0;
        for (ControllerBean controller : activeControllers) {
            double tmpCPULoad = state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage();
            double cpuNormalizingFactor = 40 / controller.getNumCPUs();
            tmpCPULoad = tmpCPULoad * cpuNormalizingFactor;
            averageCPULoad += tmpCPULoad;
        }

        averageCPULoad = averageCPULoad / numActiveControllers;

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
            runCPManMastershipAlgorithm(state);
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
        runCPManMastershipAlgorithm(state);
    }

    public void runScaleIn(ControllerBean targetController, State state) {
        CPManMastership mastership = new CPManMastership();
        ControllerScaling scaling = new ControllerScaling();

        if (mastership.getActiveControllers().size() == 3) {
            runCPManMastershipAlgorithm(state);
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
