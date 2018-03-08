package Mastership;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Utils.Connection.RESTConnection;
import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

import static Database.Configure.Configuration.RESTURL_DOMASTERSHIP;

abstract class AbstractMastership implements Mastership{
    protected mastershipType mastershipName;

    public mastershipType getMastershipName() {
        return mastershipName;
    }

    public void setMastershipName(mastershipType mastershipName) {
        this.mastershipName = mastershipName;
    }

    public void changeMastership(String dpid, ControllerBean targetController) {
        JsonObject rootObj = new JsonObject();
        rootObj.add("deviceId", dpid);
        rootObj.add("nodeId", targetController.getControllerId());
        rootObj.add("role", "MASTER");

        RESTConnection restConn = new RESTConnection();
        restConn.putCommandToUser(targetController, RESTURL_DOMASTERSHIP, rootObj);
    }

    public void changeMultipleMastership(HashMap<String, ArrayList<String>> topology) {

        for (String controllerId : topology.keySet()) {
            ControllerBean tmpControllerBean = Configuration.getInstance().getControllerBeanWithId(controllerId);
            for (String dpid : topology.get(controllerId)) {
                changeMastership(dpid, tmpControllerBean);
            }
        }
    }
}
