package Mastership;

import Beans.ControllerBean;
import com.eclipsesource.json.JsonObject;

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
    }
}
