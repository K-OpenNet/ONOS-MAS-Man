package DecisionMaker;

import Database.Configure.Configuration;
import Database.Tables.State;
import Mastership.CPManMastership;

import java.util.ArrayList;

public class NoScalingCPManAlgorithm extends AbstractDecisionMaker implements DecisionMaker {
    public NoScalingCPManAlgorithm() {
        decisionMakerName = decisionMakerType.NOSCALING_CPMAN;
    }

    @Override
    public void runDecisionMakerAlgorithm(int currentTimeIndex, ArrayList<State> dbDump) {

        if (currentTimeIndex % Configuration.NOSCALING_CPMAN_PERIOD == 0) {
            runCPManMastershipAlgorithm(dbDump);
        }

    }

    public void runCPManMastershipAlgorithm(ArrayList<State> dbDump) {
        CPManMastership mastership = new CPManMastership();
        mastership.runMastershipAlgorithm(dbDump);
    }
}
