package DecisionMaker;

import Database.Tables.State;
import Mastership.CPManMastership;

import java.util.ArrayList;

public class NetworkingScalingAlgorithm extends AbstractDecisionMaker implements DecisionMaker {
    public NetworkingScalingAlgorithm() {
        decisionMakerName = decisionMakerType.SCALING_NETWORK;
    }

    @Override
    public void runDecisionMakerAlgorithm(int currentTimeIndex, ArrayList<State> dbDump) {

    }

    public void runCPManMastershipAlgorithm(State state) {
        CPManMastership mastership = new CPManMastership();
        mastership.runMastershipAlgorithm(state);
    }
}
