package DecisionMaker;

import Database.Tables.State;
import Mastership.EqualizingMastership;

import java.util.ArrayList;

public class NoScalingEqualizingAlgorithm extends AbstractDecisionMaker implements DecisionMaker {
    public NoScalingEqualizingAlgorithm() {
        decisionMakerName = decisionMakerType.NOSCALING;
    }

    @Override
    public void runDecisionMakerAlgorithm(int currentTimeIndex, ArrayList<State> dbDump) {
        EqualizingMastership mastership = new EqualizingMastership();
        mastership.runMastershipAlgorithm(new State());
    }
}
