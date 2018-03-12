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

        if (currentTimeIndex == 0) {
            runCPManMastershipAlgorithm(dbDump.get(0));
        }
        else if (currentTimeIndex % Configuration.NOSCALING_CPMAN_PERIOD == 0) {
            ArrayList<State> targetStates = new ArrayList<>();

            int startPoint = currentTimeIndex - Configuration.NOSCALING_CPMAN_PERIOD + 1;
            int endPoint = currentTimeIndex;

            for (int index = startPoint; index <= endPoint; index++) {
                targetStates.add(dbDump.get(index));
            }

            State state = mergeStates(targetStates);
            runCPManMastershipAlgorithm(state);
        }

    }

    public void runCPManMastershipAlgorithm(State state) {
        CPManMastership mastership = new CPManMastership();
        mastership.runMastershipAlgorithm(state);
    }
}
