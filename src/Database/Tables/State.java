package Database.Tables;

import Beans.ControllerBean;
import Beans.SwitchBean;
import Database.Tuples.ComputingResourceTuple;
import Database.Tuples.ControlPlaneTuple;
import Database.Tuples.MastershipTuple;

import java.util.HashMap;

public class State {

    // Key: controllerKey, value: MastershipTuple
    private HashMap<String, MastershipTuple> mastershipTuples;
    // Key: controllerKey, value: ComputingResourceTuple
    private HashMap<String, ComputingResourceTuple> computingResourceTuples;
    // key: switchKey, value: ControlPlaneTuple
    private HashMap<String, ControlPlaneTuple> controlPlaneTuples;

    public State() {
        mastershipTuples = new HashMap<>();
        computingResourceTuples = new HashMap<>();
        controlPlaneTuples = new HashMap<>();
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

    public void addControlPlaneTuple(SwitchBean sw, ControlPlaneTuple controlPlaneTuple) {
        if(controlPlaneTuples.containsKey(sw.getBeanKey())) {
            throw new ControlPlaneTupleSanityException();
        }

        controlPlaneTuples.put(sw.getBeanKey(), controlPlaneTuple);
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

    public HashMap<String, ControlPlaneTuple> getControlPlaneTuples() {
        return controlPlaneTuples;
    }

    public void setControlPlaneTuples(HashMap<String, ControlPlaneTuple> controlPlaneTuples) {
        this.controlPlaneTuples = controlPlaneTuples;
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

class ControlPlaneTupleSanityException extends RuntimeException {
    public ControlPlaneTupleSanityException() {
        super();
    }

    public ControlPlaneTupleSanityException(String message) {
        super(message);
    }
}