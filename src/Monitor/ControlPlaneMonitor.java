package Monitor;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tuples.ControlPlaneTuple;
import Utils.Connection.RESTConnection;
import Utils.Parser.JsonParser;

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

    public HashMap<String, HashMap<String, ControlPlaneTuple>> monitorControlPlane() {
        HashMap<String, HashMap<String, ControlPlaneTuple>> results = new HashMap<>();

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {

            if (results.containsKey(controller.getBeanKey())) {
                throw new ControlPlaneMonitoringSanityException();
            }

            results.put(controller.getBeanKey(), monitorControlPlaneForEachController(controller));
        }

        return results;
    }

    public HashMap<String, ControlPlaneTuple> monitorControlPlaneForEachController(ControllerBean controller) {
        String tmpRawResults = monitorRawControlPlane(controller);

        JsonParser parser = new JsonParser();

        return parser.parseControlPlaneMonitoringResult(controller, tmpRawResults);
    }

}

class ControlPlaneMonitoringSanityException extends RuntimeException {
    public ControlPlaneMonitoringSanityException() {
        super();
    }

    public ControlPlaneMonitoringSanityException(String message) {
        super(message);
    }
}
