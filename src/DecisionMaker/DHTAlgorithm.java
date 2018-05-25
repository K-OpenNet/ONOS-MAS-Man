package DecisionMaker;

import Database.Configure.Configuration;
import Database.Tables.State;

import java.util.ArrayList;

public class DHTAlgorithm extends AbstractDecisionMaker implements DecisionMaker {

    public DHTAlgorithm() {
        decisionMakerName = decisionMakerType.SCALING_DHT;
    }

    @Override
    public void runDecisionMakerAlgorithm(int currentTimeIndex, ArrayList<State> dbDump) {

    }
}
