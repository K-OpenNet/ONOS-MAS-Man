package DecisionMaker;

public interface DecisionMaker {
    enum decisionMakerType {
        DCORAL, SCALING_CPU, SCALING_NETWORK, NOSCALING_CPMAN;
    }

    void runDecisionMakerAlgorithm(int currentTimeIndex);
}
