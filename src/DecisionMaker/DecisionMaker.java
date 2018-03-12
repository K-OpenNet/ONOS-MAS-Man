package DecisionMaker;

import Database.Tables.State;

import java.util.ArrayList;

public interface DecisionMaker {
    enum decisionMakerType {
        DCORAL, SCALING_CPU, SCALING_NETWORK, NOSCALING_CPMAN;
    }

    void runDecisionMakerAlgorithm(int currentTimeIndex, ArrayList<State> dbDump);
}
