package Mastership;

import Beans.ControllerBean;
import Beans.SwitchBean;
import com.eclipsesource.json.JsonObject;

abstract class AbstractMastership implements Mastership{
    protected mastershipType mastershipName;

    public mastershipType getMastershipName() {
        return mastershipName;
    }

    public void setMastershipName(mastershipType mastershipName) {
        this.mastershipName = mastershipName;
    }

    public void changeMastership(SwitchBean sw, ControllerBean targetController) {
        JsonObject rootObj = new JsonObject();
        rootObj.add("deviceId", sw.getDpid());
        rootObj.add("nodeId", targetController.getControllerId());
        rootObj.add("role", "MASTER");
    }
}
