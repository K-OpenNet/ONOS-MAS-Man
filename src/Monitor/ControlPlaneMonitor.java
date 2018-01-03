package Monitor;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tables.Database;
import Database.Tuples.ControlPlaneTuple;
import Utils.Connection.RESTConnection;
import Utils.Parser.JsonParser;
import org.projectfloodlight.openflow.protocol.OFType;

import java.util.HashMap;

import static Database.Configure.Configuration.RESTURL_CPMESSAGES;

public class ControlPlaneMonitor extends AbstractMonitor implements Monitor {
    public ControlPlaneMonitor() {
        monitorName = monitorType.CONTROLPLANEMONITOR;
    }

    public String monitorRawControlPlane(ControllerBean controller) {
        RESTConnection restConn = new RESTConnection();

        return restConn.sendCommandToUser(controller, RESTURL_CPMESSAGES);
    }

    public HashMap<String, ControlPlaneTuple> monitorControlPlaneInit() {

        JsonParser parser = new JsonParser();
        HashMap<String, ControlPlaneTuple> results = new HashMap<>();

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            String tmpRawResult = monitorRawControlPlane(controller);
            ControlPlaneTuple tmpResult = parser.parseControlPlaneMonitoringResult(controller, tmpRawResult);

            if (tmpResult.getDpid() == null || tmpResult.getDpid() == "") {
                System.out.println("DPID for switch, " + tmpResult.toString() + " is not set");
                throw new NullPointerException();
            }

            if (results.containsKey(tmpResult.getDpid())) {
                combineTwoTuples(tmpResult, results.get(tmpResult.getDpid()));
            } else {
                results.put(tmpResult.getDpid(), tmpResult);
            }
        }

        return results;
    }

    public HashMap<String, ControlPlaneTuple> monitorControlPlane(int timeIndex) {

        // TODO: change to Thread
        if (timeIndex == 0) {
            System.out.println("Not available this function when timeIndex == 0");
            throw new WrongTimeIndexException();
        }

        Database db = Database.getInstance();

        HashMap<String, ControlPlaneTuple> prevResults = db.getAllTuples(timeIndex).getControlPlaneTuples();
        HashMap<String, ControlPlaneTuple> currentResults = monitorControlPlaneInit();

        for (String dpid : currentResults.keySet()) {

            if (!prevResults.keySet().contains(dpid)) {
                continue;
            }

            currentResults.get(dpid).setOutdatedControlTrafficResults((HashMap<OFType, Long>) prevResults.get(dpid).getOutdatedControlTrafficResults().clone());
            currentResults.get(dpid).setOutdatedControlTrafficBytesResults((HashMap<OFType, Long>) prevResults.get(dpid).getOutdatedControlTrafficBytesResults().clone());

            ControlPlaneTuple tmpTuple = currentResults.get(dpid);
            ControlPlaneTuple prevTuple = prevResults.get(dpid);
            for (OFType type : OFType.values()) {
                long currentControlTrafficRawResult = tmpTuple.getControlTrafficRawResults().get(type)
                        + tmpTuple.getOutdatedControlTrafficResults().get(type);
                long currentControlTrafficByteRawResults = tmpTuple.getControlTrafficByteRawResults().get(type)
                        + tmpTuple.getOutdatedControlTrafficBytesResults().get(type);
                long prevControlTrafficRawResult = prevTuple.getControlTrafficRawResults().get(type)
                        + prevTuple.getOutdatedControlTrafficResults().get(type);
                long prevControlTrafficByteRawResults = prevTuple.getControlTrafficByteRawResults().get(type)
                        + prevTuple.getOutdatedControlTrafficBytesResults().get(type);

                long tmpControlTrafficResult = currentControlTrafficRawResult - prevControlTrafficRawResult;
                long tmpControlTrafficByteResult = currentControlTrafficByteRawResults - prevControlTrafficByteRawResults;

                if (tmpControlTrafficByteResult < 0 || tmpControlTrafficResult < 0) {
                    throw new ControlPlaneMonitoringResultSanityException();
                }

                tmpTuple.getControlTrafficResults().replace(type, tmpControlTrafficResult);
                tmpTuple.getControlTrafficByteResults().replace(type, tmpControlTrafficByteResult);
            }

        }

        return currentResults;
    }

    public ControlPlaneTuple combineTwoTuples(ControlPlaneTuple origTup, ControlPlaneTuple newTup) {
        for (OFType type : OFType.values()) {
            long controlTrafficResult = origTup.getControlTrafficResults().get(type)
                    + newTup.getControlTrafficResults().get(type);
            long controlTrafficBytesResult = origTup.getControlTrafficByteResults().get(type)
                    + newTup.getControlTrafficByteResults().get(type);
            long controlTrafficRawResult = origTup.getControlTrafficRawResults().get(type)
                    + newTup.getControlTrafficRawResults().get(type);
            long controlTrafficRawByteResult = origTup.getControlTrafficByteRawResults().get(type)
                    + newTup.getControlTrafficByteRawResults().get(type);

            origTup.getControlTrafficResults().replace(type, controlTrafficResult);
            origTup.getControlTrafficByteResults().replace(type, controlTrafficBytesResult);
            origTup.getControlTrafficRawResults().replace(type, controlTrafficRawResult);
            origTup.getControlTrafficByteRawResults().replace(type, controlTrafficRawByteResult);
        }

        return origTup;
    }
}

class WrongTimeIndexException extends RuntimeException {
    public WrongTimeIndexException() {
        super();
    }

    public WrongTimeIndexException(String message) {
        super(message);
    }
}

class ControlPlaneMonitoringResultSanityException extends RuntimeException {
    public ControlPlaneMonitoringResultSanityException() {
        super();
    }

    public ControlPlaneMonitoringResultSanityException(String message) {
        super(message);
    }
}
