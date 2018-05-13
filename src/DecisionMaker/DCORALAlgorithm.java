package DecisionMaker;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tables.State;
import Mastership.CPManMastership;
import Scaling.CPUScaling;

import java.util.ArrayList;

public class DCORALAlgorithm extends AbstractDecisionMaker implements DecisionMaker {

    public DCORALAlgorithm() {
        decisionMakerName = decisionMakerType.DCORAL;
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
            double cpuNormalizeFactor = 40/controller.getNumCPUs();
            tmpCPULoad = tmpCPULoad * cpuNormalizeFactor;

            if (((double) Configuration.SCALING_THRESHOLD_HIGHEST / 100) > tmpCPULoad) {
                incVirtualCPUs(2, controller);
            } else if (((double) Configuration.SCALING_THRESHOLD_UPPER / 100) > tmpCPULoad) {
                incVirtualCPUs(1, controller);
            } else if (((double) Configuration.SCALING_THRESHOLD_LOWER / 100) < tmpCPULoad) {
                decVirtualCPUs(1, controller);
            } else if (((double) Configuration.SCALING_THRESHOL_LOWEST / 100) < tmpCPULoad) {
                decVirtualCPUs(2, controller);
            }
        }

    }

    public void runCPManMastershipAlgorithm(State state) {
        CPManMastership mastership = new CPManMastership();
        mastership.runMastershipAlgorithm(state);
    }
}
