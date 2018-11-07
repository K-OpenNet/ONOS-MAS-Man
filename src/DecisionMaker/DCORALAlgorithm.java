package DecisionMaker;

import Beans.ControllerBean;
import Controller.Controller;
import Database.Configure.Configuration;
import Database.Tables.State;
import Mastership.CPManMastership;
import Scaling.CPUScaling;

import java.util.ArrayList;

import static Database.Configure.Configuration.LAST_SCALEIN_TIME_INDEX;
import static Database.Configure.Configuration.LAST_SCALEOUT_TIME_INDEX;
import static Database.Configure.Configuration.NUM_BUBBLE;

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
            double rawTmpCPULoad = tmpCPULoad;
            double cpuNormalizeFactor = 40/controller.getNumCPUs();
            tmpCPULoad = tmpCPULoad * cpuNormalizeFactor;

            if (tmpCPULoad > 100.0) {
                tmpCPULoad = 100;
            }

            if (Configuration.SCALING_THRESHOLD_HIGHEST < tmpCPULoad) {
                if (Controller.getTimeIndex() - LAST_SCALEIN_TIME_INDEX <= NUM_BUBBLE && LAST_SCALEIN_TIME_INDEX != -1) {

                } else if (controller.getNumCPUs() < 17) {
                    System.out.println(controller.getControllerId() + ": " + "scaling out 2 cpus -- " + tmpCPULoad + " % / Thr: " + (Configuration.SCALING_THRESHOLD_UPPER) + "%");
                    incVirtualCPUs(2, controller);
                    LAST_SCALEOUT_TIME_INDEX = Controller.getTimeIndex();
                } else if (controller.getNumCPUs() < 18) {
                    System.out.println(controller.getControllerId() + ": " + "scaling out 1 cpu -- " + tmpCPULoad + " % / Thr: " + (Configuration.SCALING_THRESHOLD_UPPER) + "%");
                    incVirtualCPUs(1, controller);
                    LAST_SCALEOUT_TIME_INDEX = Controller.getTimeIndex();
                }
            } else if (Configuration.SCALING_THRESHOLD_UPPER < tmpCPULoad) {
                if (Controller.getTimeIndex() - LAST_SCALEIN_TIME_INDEX <= NUM_BUBBLE && LAST_SCALEIN_TIME_INDEX != -1) {

                } else if (controller.getNumCPUs() < 18) {
                    System.out.println(controller.getControllerId() + ": " + "scaling out 1 cpu -- " + tmpCPULoad + " % / Thr: " + (Configuration.SCALING_THRESHOLD_UPPER) + "%");
                    incVirtualCPUs(1, controller);
                    LAST_SCALEOUT_TIME_INDEX = Controller.getTimeIndex();
                }
            } else if (Configuration.SCALING_THRESHOLD_LOWER > tmpCPULoad) {
                if (Controller.getTimeIndex() - LAST_SCALEOUT_TIME_INDEX <= NUM_BUBBLE && LAST_SCALEOUT_TIME_INDEX != -1) {

                } else if (controller.getNumCPUs() > 2) {
                    int numElemCPUs = 1;
                    cpuNormalizeFactor = 40/(controller.getNumCPUs()-numElemCPUs);
                    rawTmpCPULoad = rawTmpCPULoad * cpuNormalizeFactor;
                    if (rawTmpCPULoad < Configuration.SCALING_THRESHOLD_UPPER) {
                        System.out.println(controller.getControllerId() + ": " + "scaling in 1 cpu -- " + tmpCPULoad + " % / Thr: " + (Configuration.SCALING_THRESHOLD_LOWER) + " %");
                        decVirtualCPUs(numElemCPUs, controller);
                        LAST_SCALEIN_TIME_INDEX = Controller.getTimeIndex();
                    }
                }
            } else if (Configuration.SCALING_THRESHOL_LOWEST > tmpCPULoad) {
                if (Controller.getTimeIndex() - LAST_SCALEOUT_TIME_INDEX <= NUM_BUBBLE && LAST_SCALEOUT_TIME_INDEX != -1) {

                } else if (controller.getNumCPUs() > 3) {
                    int numElemCPUs = 2;
                    cpuNormalizeFactor = 40/(controller.getNumCPUs()-numElemCPUs);
                    rawTmpCPULoad = rawTmpCPULoad * cpuNormalizeFactor;
                    if (rawTmpCPULoad < Configuration.SCALING_THRESHOLD_UPPER) {
                        System.out.println(controller.getControllerId() + ": " + "scaling in 2 cpus -- " + tmpCPULoad + " % / Thr: " + (Configuration.SCALING_THRESHOLD_LOWER) + " %");
                        decVirtualCPUs(numElemCPUs, controller);
                        LAST_SCALEIN_TIME_INDEX = Controller.getTimeIndex();
                    }
                } else if (controller.getNumCPUs() > 2) {
                    int numElemCPUs = 1;
                    cpuNormalizeFactor = 40/(controller.getNumCPUs()-numElemCPUs);
                    rawTmpCPULoad = rawTmpCPULoad * cpuNormalizeFactor;
                    if (rawTmpCPULoad < Configuration.SCALING_THRESHOLD_UPPER) {
                        System.out.println(controller.getControllerId() + ": " + "scaling in 1 cpu -- " + tmpCPULoad + " % / Thr: " + (Configuration.SCALING_THRESHOLD_LOWER) + " %");
                        decVirtualCPUs(1, controller);
                        LAST_SCALEIN_TIME_INDEX = Controller.getTimeIndex();
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
