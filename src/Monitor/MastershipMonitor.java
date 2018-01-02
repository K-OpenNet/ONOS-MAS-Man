package Monitor;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tuples.MastershipTuple;
import Utils.Connection.RESTConnection;
import Utils.Parser.JsonParser;

import java.util.HashMap;

import static Database.Configure.Configuration.RESTURL_GETMASTERSHIPINFO;

public class MastershipMonitor extends AbstractMonitor implements Monitor {
    public MastershipMonitor() {
        monitorName = monitorType.MASTERSHIPMONITOR;
    }

    public String monitorRawMastership(ControllerBean controller) {

        RESTConnection restConn = new RESTConnection();

        return restConn.sendCommandToUser(controller, RESTURL_GETMASTERSHIPINFO);
    }

    public HashMap<String, MastershipTuple> monitorMastership() {

        JsonParser parser = new JsonParser();
        RESTConnection restConn = new RESTConnection();
        HashMap<String, MastershipTuple> results = new HashMap<>();

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {

            if (results.containsKey(controller.getBeanKey())) {
                System.out.println(controller.getBeanKey() + " is duplicated in results array. see MastershipMonitor.");
                throw new MastershipSanityException();
            }

            // Get switches which are serviced by this controller as raw data
            // ToDo: it will be defined as a thread -- too long getting time
            String tmpRawResult = restConn.sendCommandToUser(controller, RESTURL_GETMASTERSHIPINFO);

            results.put(controller.getBeanKey(), parser.parseMastershipMonitoringResults(tmpRawResult));
        }

        return results;
    }
}

class MastershipSanityException extends RuntimeException {
    public MastershipSanityException() {
        super();
    }

    public MastershipSanityException(String message) {
        super(message);
    }
}
