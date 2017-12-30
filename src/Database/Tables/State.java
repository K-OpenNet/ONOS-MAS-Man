package Database.Tables;

import Beans.ControllerBean;
import Beans.SwitchBean;
import Database.Tuples.ComputingResourceTuple;
import Database.Tuples.MastershipTuple;

import java.util.HashMap;

public class State {

    // Key: controllerKey, value: MastershipTuple
    private HashMap<String, MastershipTuple> mastershipTuples;
    private HashMap<String, ComputingResourceTuple> computingResourceTuples;

    public State() {
        mastershipTuples = new HashMap<>();
        computingResourceTuples = new HashMap<>();
    }

    public void addMastershipTuple(ControllerBean controller, SwitchBean sw) {
        if(!mastershipTuples.containsKey(controller.getBeanKey())) {
            mastershipTuples.put(controller.getBeanKey(), new MastershipTuple());
        }

        if(mastershipTuples.get(controller.getBeanKey()).hasSwitch(sw.getBeanKey())) {
            throw new MastershipTupleSanityException();
        }

        mastershipTuples.get(controller.getBeanKey()).addSwitch(sw.getBeanKey());
    }

    public void addComputingResourceTuple(ControllerBean controller, ComputingResourceTuple computingResourceTuple) {
        if(computingResourceTuples.containsKey(controller.getBeanKey())) {
            throw new ComputingResourceTupleSanityException();
        }

        computingResourceTuples.put(controller.getBeanKey(), computingResourceTuple);
    }

    public HashMap<String, MastershipTuple> getMastershipTuples() {
        return mastershipTuples;
    }

    public void setMastershipTuples(HashMap<String, MastershipTuple> mastershipTuples) {
        this.mastershipTuples = mastershipTuples;
    }

    public HashMap<String, ComputingResourceTuple> getComputingResourceTuples() {
        return computingResourceTuples;
    }

    public void setComputingResourceTuples(HashMap<String, ComputingResourceTuple> computingResourceTuples) {
        this.computingResourceTuples = computingResourceTuples;
    }
}

class MastershipTupleSanityException extends RuntimeException {
    public MastershipTupleSanityException() {
        super();
    }

    public MastershipTupleSanityException(String message) {
        super(message);
    }
}

class ComputingResourceTupleSanityException extends RuntimeException {
    public ComputingResourceTupleSanityException() {
        super();
    }

    public ComputingResourceTupleSanityException(String message) {
        super(message);
    }
}