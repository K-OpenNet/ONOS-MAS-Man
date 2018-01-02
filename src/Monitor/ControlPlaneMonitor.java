package Monitor;

import Beans.ControllerBean;
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

        return results;
    }

    public HashMap<String, ControlPlaneTuple> monitorControlPlane(int timeIndex) {

        Database db = Database.getInstance();

        HashMap<String, ControlPlaneTuple> prevResults = db.getAllTuples(timeIndex).getControlPlaneTuples();
        HashMap<String, ControlPlaneTuple> currentResults = monitorControlPlaneInit();

        return currentResults;
    }
}
