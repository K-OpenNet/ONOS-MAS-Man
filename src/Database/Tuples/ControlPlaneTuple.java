package Database.Tuples;

import org.projectfloodlight.openflow.protocol.OFType;

import java.util.HashMap;

public class ControlPlaneTuple extends AbstractTuple implements Tuple {

    private HashMap<OFType, Long> controlTrafficResults;
    private HashMap<OFType, Long> controlTrafficByteResults;

    private String dpid;

    public ControlPlaneTuple() {
        this.tupleName = tupleType.CONTROLPLANETUPLE;

        this.controlTrafficResults = initHashMaps();
        this.controlTrafficByteResults = initHashMaps();
    }

    public HashMap<OFType, Long> initHashMaps() {
        HashMap<OFType, Long> maps = new HashMap<>();

        for (OFType type : OFType.values()) {
            maps.put(type, (long) 0);
        }

        return maps;
    }

    public long totalControlTrafficMessages() {
        long results = 0;

        for (OFType type : OFType.values()) {
            results += controlTrafficResults.get(type);
        }

        return results;
    }

    public long totalControlTrafficBytes() {
        long results = 0;

        for (OFType type : OFType.values()) {
            results += controlTrafficByteResults.get(type);
        }

        return results;
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

    public String getDpid() {
        return dpid;
    }

    public void setDpid(String dpid) {
        this.dpid = dpid;
    }
}
