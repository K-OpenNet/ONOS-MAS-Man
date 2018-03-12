package DecisionMaker;

import Beans.ControllerBean;
import Database.Tables.State;
import Mastership.EqualizingMastership;
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

    public void equalizingMastership() {
        EqualizingMastership mastership = new EqualizingMastership();
        mastership.runMastershipAlgorithm(new ArrayList<State>());
    }

    @Override
    public void runDecisionMakerAlgorithm(int currentTimeIndex, ArrayList<State> dbDump) {

    }
}
