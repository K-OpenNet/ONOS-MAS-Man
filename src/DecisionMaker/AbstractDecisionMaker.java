package DecisionMaker;


import Database.Tables.State;
import Database.Tuples.ComputingResourceTuple;
import Database.Tuples.ControlPlaneTuple;
import Database.Tuples.MastershipTuple;

import java.util.ArrayList;
import java.util.HashMap;

abstract class AbstractDecisionMaker implements DecisionMaker {
    protected decisionMakerType decisionMakerName;

    public decisionMakerType getDecisionMakerName() {
        return decisionMakerName;
    }

    public void setDecisionMakerName(decisionMakerType decisionMakerName) {
        this.decisionMakerName = decisionMakerName;
    }

    public State mergeStates(ArrayList<State> states) {
        State result = null;

        for (int lastIndex1 = states.size() - 1; lastIndex1 <= 0; lastIndex1--) {
            if (result == null) {
                result = new State();
                result.setMastershipTuples((HashMap<String, MastershipTuple>) states.get(lastIndex1).getMastershipTuples().clone());
                result.setComputingResourceTuples((HashMap<String, ComputingResourceTuple>) states.get(lastIndex1).getComputingResourceTuples().clone());
                result.setControlPlaneTuples((HashMap<String, HashMap<String, ControlPlaneTuple>>) states.get(lastIndex1).getControlPlaneTuples().clone());
                result.setNumCPUsTuples((HashMap<String, Integer>) states.get(lastIndex1).getNumCPUsTuples().clone());
            } else {
                // ToDo: merge two states with following strategies
                // mastership: last -- do not touch
                // num cpus: last -- do not touch
                // computing resource: summation and then average
                HashMap<String, ComputingResourceTuple> tmpComputingResource = states.get(lastIndex1).getComputingResourceTuples();
                for (String controllerId : tmpComputingResource.keySet()) {
                    ComputingResourceTuple tmpComputingResourceTuple = tmpComputingResource.get(controllerId);
                    result.getComputingResourceTuples().get(controllerId).getCpuUsageUser().addAll(tmpComputingResourceTuple.getCpuUsageUser());
                    result.getComputingResourceTuples().get(controllerId).getCpuUsageKernel().addAll(tmpComputingResourceTuple.getCpuUsageKernel());
                    result.getComputingResourceTuples().get(controllerId).getRamUsage().addAll(tmpComputingResourceTuple.getRamUsage());
                    result.getComputingResourceTuples().get(controllerId).getNetRx().addAll(tmpComputingResourceTuple.getNetRx());
                    result.getComputingResourceTuples().get(controllerId).getNetTx().addAll(tmpComputingResourceTuple.getNetTx());
                }
                // control plane: summation
                HashMap<String, HashMap<String, ControlPlaneTuple>> tmpControlPlane = states.get(lastIndex1).getControlPlaneTuples();
            }
        }
        return result;
    }

}
