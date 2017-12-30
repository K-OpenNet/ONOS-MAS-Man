package Database.Tables;

import Beans.ControllerBean;
import Beans.SwitchBean;
import Database.Tuples.MastershipTuple;

import java.util.HashMap;

public class State {

    // Key: controllerKey, value: MastershipTuple
    private HashMap<String, MastershipTuple> mastershipTuples;

    public State() {
        mastershipTuples = new HashMap<>();
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

    public HashMap<String, MastershipTuple> getMastershipTuples() {
        return mastershipTuples;
    }

    public void setMastershipTuples(HashMap<String, MastershipTuple> mastershipTuples) {
        this.mastershipTuples = mastershipTuples;
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