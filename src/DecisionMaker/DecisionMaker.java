package DecisionMaker;

import Database.Tables.State;

import java.util.ArrayList;

public interface DecisionMaker {
    enum decisionMakerType {
        SDCORAL, DCORAL, SCALING_CPU, SCALING_NETWORK, SCALING_DHT, NOSCALING_CPMAN, NOSCALING, HECP;
    }

    void runDecisionMakerAlgorithm(int currentTimeIndex, ArrayList<State> dbDump);
}
