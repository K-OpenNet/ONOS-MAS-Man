package DecisionMaker;

import Database.Configure.Configuration;
import Database.Tables.State;

import java.util.ArrayList;

public class HybridECP extends AbstractDecisionMaker implements DecisionMaker {

    public HybridECP() {
        decisionMakerName = decisionMakerType.HECP;
    }

    @Override
    public void runDecisionMakerAlgorithm(int currentTimeIndex, ArrayList<State> dbDump) {

        if (currentTimeIndex == 0) {
            return;
        }

        ArrayList<State> targetStates = new ArrayList<>();

        int startPoint = currentTimeIndex - Configuration.NOSCALING_CPMAN_PERIOD + 1;
        int endPoint = currentTimeIndex;

        for (int index = startPoint; index <= endPoint; index++) {
            targetStates.add(dbDump.get(index));
        }

        State state = mergeStates(targetStates);
    }
}
