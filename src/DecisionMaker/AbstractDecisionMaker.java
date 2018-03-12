package DecisionMaker;


import Database.Tables.State;

import java.util.ArrayList;

abstract class AbstractDecisionMaker implements DecisionMaker {
    protected decisionMakerType decisionMakerName;

    public decisionMakerType getDecisionMakerName() {
        return decisionMakerName;
    }

    public void setDecisionMakerName(decisionMakerType decisionMakerName) {
        this.decisionMakerName = decisionMakerName;
    }

    public State mergeStates(ArrayList<State> states) {
        State result = new State();

        return result;
    }

}
