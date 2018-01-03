package Database.Configure;

import Beans.ControllerBean;
import Beans.PMBean;

import java.util.ArrayList;
import java.util.HashMap;

public class Configuration {
    private static Configuration ourInstance = new Configuration();

    public static Configuration getInstance() {
        return ourInstance;
    }

    private Configuration() {
        controllers = new ArrayList<>();
        pms = new ArrayList<>();
        relationships = new HashMap<>();
    }

    // Experimental variables
    public static final int MONITORING_PERIOD = 3;

    // CMDs
    public static final String CMD_CPU_BITMAP_TEMPLATE = "/sys/devices/system/cpu/cpu<index>/online";
    public static final String CMD_COMPUTING_RESOURCE_QUERY = "vboxmanage metrics query '*' CPU/Load/User,CPU/Load/Kernel,RAM/Usage/Used,Net/Rate/Rx,Net/Rate/Tx";

    // URLs for REST API
    public static final String RESTURL_PREFIX = "http://<controllerIP>:<controllerPort>";
    public static final String RESTURL_GETMASTERSHIPINFO =  RESTURL_PREFIX + "/onos/v1/mastership/<controllerID>/device";
    public static final String RESTURL_DOMASTERSHIP = RESTURL_PREFIX + "/onos/v1/mastership";
    public static final String RESTURL_CPMESSAGES = RESTURL_PREFIX + "/onos/cpmanrt/controlmessages/messages";
    public static final String RESTURL_DOSCALEOUT = RESTURL_PREFIX + "/onos/cpmanrt/controlmessages/scaleout/<controllerID>";
    public static final String RESTURL_DOSCALEIN = RESTURL_PREFIX + "/onos/cpmanrt/controlmessages/scalein/<controllerID>";
    public static final String RESTURL_DOEQUALIZE = RESTURL_PREFIX + "/onos/cpmanrt/controlmessages/equalizing";

    private ArrayList<ControllerBean> controllers;
    private ArrayList<PMBean> pms;
    private HashMap<PMBean, ArrayList<ControllerBean>> relationships;

    public ControllerBean getControllerBean (String name) {
        for (int index = 0; index < controllers.size(); index++) {
            if (controllers.get(index).getName().equals(name)) {
                return controllers.get(index);
            }
        }

        throw new NullPointerException();
    }

    public PMBean getPMBean (String ipAddr) {
        for (int index = 0; index < pms.size(); index++) {
            if(pms.get(index).getIpAddr().equals(ipAddr)) {
                return pms.get(index);
            }
        }

        throw new NullPointerException();
    }

    public ArrayList<ControllerBean> getControllers() {
        return controllers;
    }

    public void setControllers(ArrayList<ControllerBean> controllers) {
        this.controllers = controllers;
    }

    public ArrayList<PMBean> getPms() {
        return pms;
    }

    public void setPms(ArrayList<PMBean> pms) {
        this.pms = pms;
    }

    public HashMap<PMBean, ArrayList<ControllerBean>> getRelationships() {
        return relationships;
    }

    public void setRelationships(HashMap<PMBean, ArrayList<ControllerBean>> relationships) {
        this.relationships = relationships;
    }
}
