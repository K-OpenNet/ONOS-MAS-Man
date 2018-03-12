package DecisionMaker;

import Database.Configure.Configuration;
import Mastership.CPManMastership;

public class NoScalingCPManAlgorithm extends AbstractDecisionMaker implements DecisionMaker {
    public NoScalingCPManAlgorithm() {
        decisionMakerName = decisionMakerType.NOSCALING_CPMAN;
    }

    @Override
    public void runDecisionMakerAlgorithm(int currentTimeIndex) {

        if (currentTimeIndex % Configuration.NOSCALING_CPMAN_PERIOD == 0) {
            runCPManMastershipAlgorithm();
        }

    }

    public void runCPManMastershipAlgorithm() {
        CPManMastership mastership = new CPManMastership();
        mastership.runMastershipAlgorithm();
    }
}
