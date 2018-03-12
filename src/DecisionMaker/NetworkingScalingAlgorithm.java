package DecisionMaker;

import Database.Tables.State;

import java.util.ArrayList;

public class NetworkingScalingAlgorithm extends AbstractDecisionMaker implements DecisionMaker {
    public NetworkingScalingAlgorithm() {
        decisionMakerName = decisionMakerType.SCALING_NETWORK;
    }

    @Override
    public void runDecisionMakerAlgorithm(int currentTimeIndex, ArrayList<State> dbDump) {

    }
}
