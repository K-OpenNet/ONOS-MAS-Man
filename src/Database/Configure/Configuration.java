package Database.Configure;

import Beans.ControllerBean;
import Beans.PMBean;
import DecisionMaker.DecisionMaker;

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

    // System variables
    public static final int SSH_COMMAND_RETRIES = 5;

    // Experimental variables
    public static int MONITORING_PERIOD = 3;
    public static int NOSCALING_CPMAN_PERIOD = 3;
    public static DecisionMaker.decisionMakerType DECISIONMAKER_TYPE = DecisionMaker.decisionMakerType.NOSCALING;
    public static final String DEV_MACHINE_IP_ADDR = "192.168.200.31";
    public static int SCALING_THRESHOLD_UPPER = 80;
    public static int SCALING_THRESHOLD_LOWER = 20;
    public static int SCALING_LEVEL = 1;
    public static int MIN_NUM_CONTROLLERS = 3;

    // CMDs
    public static final String CMD_CPU_BITMAP_TEMPLATE = "lscpu | grep On-line";
    public static final String CMD_COMPUTING_RESOURCE_QUERY = "vboxmanage metrics query '*' CPU/Load/User,CPU/Load/Kernel,RAM/Usage/Used,Net/Rate/Rx,Net/Rate/Tx";
    public static final String CMD_PAUSE_VM = "vboxmanage controlvm <controllerName> pause";
    public static final String CMD_RESUME_VM = "vboxmanage controlvm <controllerName> resume";
    public static final String CMD_POWEROFF_VM = "vboxmanage controlvm <controllerName> pause";
    public static final String CMD_POWERON_VM = "vboxmanage controlvm <controllerName> resume";
    public static final String CMD_DISABLE_CPU = "echo 0 > /sys/devices/system/cpu/cpu<index>/online";
    public static final String CMD_ENABLE_CPU = "echo 1 > /sys/devices/system/cpu/cpu<index>/online";
//    public static final String CMD_ONOS_SERVICE_STOP = "onos-service <controllerID> stop";
//    public static final String CMD_ONOS_SERVICE_START = "onos-service <controllerID> start";
//    public static final String CMD_CHECK_ONOS_SERVICE = "onos-secure-ssh <controllerID> | grep cpmanrt | awk \'{print $5}\'";
    public static final String CMD_ONOS_SERVICE_STOP = "/home/woojoong/workspace/AutoScalingONOS/stopONOS.sh <controllerID>";
    public static final String CMD_ONOS_SERVICE_START = "/home/woojoong/workspace/AutoScalingONOS/startONOS.sh <controllerID>";
    public static final String CMD_CHECK_ONOS_SERVICE = "/home/woojoong/workspace/AutoScalingONOS/checkONOS.sh <controllerID>";

    // URLs for REST API
    public static final String RESTURL_PREFIX = "http://<controllerIP>:<controllerPort>";
    public static final String RESTURL_GETMASTERSHIPINFO =  RESTURL_PREFIX + "/onos/v1/mastership/<controllerID>/device";
    public static final String RESTURL_DOMASTERSHIP = RESTURL_PREFIX + "/onos/v1/mastership";
    public static final String RESTURL_CPMESSAGES = RESTURL_PREFIX + "/onos/cpmanrt/controlmessages/messages";
    public static final String RESTURL_DOSCALEOUT = RESTURL_PREFIX + "/onos/cpmanrt/controlmessages/scaleout/<controllerID>";
    public static final String RESTURL_DOSCALEIN = RESTURL_PREFIX + "/onos/cpmanrt/controlmessages/scalein/<controllerID>";
    public static final String RESTURL_DOEQUALIZE = RESTURL_PREFIX + "/onos/cpmanrt/controlmessages/equalizing";

    // etc
    public static final String FILE_NAME_PREFIX = "result_<timeindex>.csv";

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

    public ControllerBean getControllerBeanWithId (String controllerId) {
        for (int index = 0; index < controllers.size(); index++) {
            if (controllers.get(index).getControllerId().equals(controllerId)) {
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
