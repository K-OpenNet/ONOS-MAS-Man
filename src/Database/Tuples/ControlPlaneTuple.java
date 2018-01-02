package Database.Tuples;

import org.projectfloodlight.openflow.protocol.OFType;

import java.util.HashMap;

public class ControlPlaneTuple extends AbstractTuple implements Tuple {

    private HashMap<OFType, Long> controlTrafficResults;
    private HashMap<OFType, Long> controlTrafficByteResults;

    private HashMap<OFType, Long> controlTrafficRawResults;
    private HashMap<OFType, Long> controlTrafficByteRawResults;

    private HashMap<OFType, Long> outdatedControlTrafficResults;
    private HashMap<OFType, Long> outdatedControlTrafficBytesResults;

    private String masterControllerID;

    public ControlPlaneTuple(String masterControllerID) {
        tupleName = tupleType.CONTROLPLANETUPLE;

        initHashMaps(controlTrafficResults);
        initHashMaps(controlTrafficByteResults);
        initHashMaps(controlTrafficRawResults);
        initHashMaps(controlTrafficByteRawResults);
        initHashMaps(outdatedControlTrafficResults);
        initHashMaps(outdatedControlTrafficBytesResults);

        this.masterControllerID = masterControllerID;
    }

    public void initHashMaps(HashMap<OFType, Long> maps) {
        maps = new HashMap<>();

        for (OFType type : OFType.values()) {
            maps.put(type, (long) 0);
        }
    }

    public HashMap<OFType, Long> getControlTrafficResults() {
        return controlTrafficResults;
    }

    public void setControlTrafficResults(HashMap<OFType, Long> controlTrafficResults) {
        this.controlTrafficResults = controlTrafficResults;
    }

    public HashMap<OFType, Long> getControlTrafficByteResults() {
        return controlTrafficByteResults;
    }

    public void setControlTrafficByteResults(HashMap<OFType, Long> controlTrafficByteResults) {
        this.controlTrafficByteResults = controlTrafficByteResults;
    }

    public HashMap<OFType, Long> getControlTrafficRawResults() {
        return controlTrafficRawResults;
    }

    public void setControlTrafficRawResults(HashMap<OFType, Long> controlTrafficRawResults) {
        this.controlTrafficRawResults = controlTrafficRawResults;
    }

    public HashMap<OFType, Long> getControlTrafficByteRawResults() {
        return controlTrafficByteRawResults;
    }

    public void setControlTrafficByteRawResults(HashMap<OFType, Long> controlTrafficByteRawResults) {
        this.controlTrafficByteRawResults = controlTrafficByteRawResults;
    }

    public HashMap<OFType, Long> getOutdatedControlTrafficResults() {
        return outdatedControlTrafficResults;
    }

    public void setOutdatedControlTrafficResults(HashMap<OFType, Long> outdatedControlTrafficResults) {
        this.outdatedControlTrafficResults = outdatedControlTrafficResults;
    }

    public HashMap<OFType, Long> getOutdatedControlTrafficBytesResults() {
        return outdatedControlTrafficBytesResults;
    }

    public void setOutdatedControlTrafficBytesResults(HashMap<OFType, Long> outdatedControlTrafficBytesResults) {
        this.outdatedControlTrafficBytesResults = outdatedControlTrafficBytesResults;
    }

    public String getMasterControllerID() {
        return masterControllerID;
    }

    public void setMasterControllerID(String masterControllerID) {
        this.masterControllerID = masterControllerID;
    }
}
