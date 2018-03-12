package DecisionMaker;

import Database.Tables.State;

import java.util.ArrayList;

public class NoScalingEqualizingAlgorithm extends AbstractDecisionMaker implements DecisionMaker {
    public NoScalingEqualizingAlgorithm() {
        decisionMakerName = decisionMakerType.NOSCALING;
    }

    @Override
    public void runDecisionMakerAlgorithm(int currentTimeIndex, ArrayList<State> dbDump) {

    }
}
