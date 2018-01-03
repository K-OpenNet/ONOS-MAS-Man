package Monitor;

import Beans.ControllerBean;
import Utils.Connection.RESTConnection;

import static Database.Configure.Configuration.RESTURL_CPMESSAGES;

public class ControlPlaneMonitor extends AbstractMonitor implements Monitor {
    public ControlPlaneMonitor() {
        monitorName = monitorType.CONTROLPLANEMONITOR;
    }

    public String monitorRawControlPlane(ControllerBean controller) {
        RESTConnection restConn = new RESTConnection();

        return restConn.sendCommandToUser(controller, RESTURL_CPMESSAGES);
    }

}
