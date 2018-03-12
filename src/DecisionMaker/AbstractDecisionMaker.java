package DecisionMaker;

import Database.Tables.State;

import java.util.ArrayList;

abstract class AbstractDecisionMaker implements DecisionMaker {
    protected decisionMakerType decisionMakerName;
    protected ArrayList<State> currentDB;

    public decisionMakerType getDecisionMakerName() {
        return decisionMakerName;
    }

    public void setDecisionMakerName(decisionMakerType decisionMakerName) {
        this.decisionMakerName = decisionMakerName;
    }

    public ArrayList<State> getCurrentDB() {
        return currentDB;
    }

    public void setCurrentDB(ArrayList<State> currentDB) {
        this.currentDB = currentDB;
    }
}
