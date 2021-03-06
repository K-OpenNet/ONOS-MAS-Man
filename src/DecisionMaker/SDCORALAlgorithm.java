package DecisionMaker;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tables.State;
import Mastership.CPManMastership;
import Scaling.CPUScaling;

import java.util.ArrayList;

public class SDCORALAlgorithm extends AbstractDecisionMaker implements DecisionMaker {

    public SDCORALAlgorithm() {
        decisionMakerName = decisionMakerType.SDCORAL;
    }

    public void incVirtualCPUs (int numCPUs, ControllerBean controller) {
        CPUScaling cpuScaling = new CPUScaling();
        cpuScaling.incMultipleVirtualCPUs(numCPUs, controller);
    }

    public void decVirtualCPUs (int numCPUs, ControllerBean controller) {
        CPUScaling cpuScaling = new CPUScaling();
        cpuScaling.decMultipleVirtaulCPUs(numCPUs, controller);
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

        for (ControllerBean controller : activeControllers) {
            double tmpCPULoad = state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage();
            double rawTmpCPULoad = tmpCPULoad;
            double cpuNormalizeFactor = 40/controller.getNumCPUs();
            tmpCPULoad = tmpCPULoad * cpuNormalizeFactor;

            if (tmpCPULoad > 100.0) {
                tmpCPULoad = 100;
            }

            System.out.println("***** Controller ID: " + controller.getControllerId() + " - " + tmpCPULoad + " (" + controller.getNumCPUs() + ")");

            if (Configuration.SCALING_THRESHOLD_UPPER < tmpCPULoad) {

                if (controller.getNumCPUs() < 18) {
                    System.out.println(controller.getControllerId() + ": " + "scaling out -- " + tmpCPULoad + " % / " + (Configuration.SCALING_THRESHOLD_UPPER) + "%");
                    incVirtualCPUs(1, controller);
                }

            } else if (Configuration.SCALING_THRESHOLD_LOWER > tmpCPULoad) {
                if (controller.getNumCPUs() > 2) {
                    int numElemCPUs = 1;
                    cpuNormalizeFactor = 40/(controller.getNumCPUs()-numElemCPUs);
                    rawTmpCPULoad = rawTmpCPULoad * cpuNormalizeFactor;
                    if (rawTmpCPULoad < Configuration.SCALING_THRESHOLD_UPPER) {
                        System.out.println(controller.getControllerId() + ": " + "scaling in -- " + tmpCPULoad + " % / " + (Configuration.SCALING_THRESHOLD_LOWER) + " %");
                        decVirtualCPUs(numElemCPUs, controller);
                    }
                }
            }
        }

    }

    public void runCPManMastershipAlgorithm(State state) {
        CPManMastership mastership = new CPManMastership();
        mastership.runMastershipAlgorithm(state);
    }
}
