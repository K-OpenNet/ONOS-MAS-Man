package DecisionMaker;

public interface DecisionMaker {
    enum decisionMakerType {
        DCORAL, SCALING_CPU, SCALING_NETWORK, NOSCALING_CPMAN, NOSCALING_EQUALIZING;
    }
}
