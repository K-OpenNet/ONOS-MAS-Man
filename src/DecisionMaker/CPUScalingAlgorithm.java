package DecisionMaker;

import Database.Tables.State;

import java.util.ArrayList;

public class CPUScalingAlgorithm extends AbstractDecisionMaker implements DecisionMaker {

    public CPUScalingAlgorithm() {
        decisionMakerName = decisionMakerType.SCALING_CPU;
    }

    @Override
    public void runDecisionMakerAlgorithm(int currentTimeIndex, ArrayList<State> dbDump) {

    }
}
