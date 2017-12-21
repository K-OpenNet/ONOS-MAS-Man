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

    // CMDs

    // URLs for REST API

    private ArrayList<ControllerBean> controllers;
    private ArrayList<PMBean> pms;
    private HashMap<PMBean, ArrayList<ControllerBean>> relationships;

    public ControllerBean getControllerBean (String controllerId, String name) {
        for (int index = 0; index < controllers.size(); index++) {
            if (controllers.get(index).getControllerId().equals(controllerId) &&
                    controllers.get(index).getName().equals(name)) {
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
