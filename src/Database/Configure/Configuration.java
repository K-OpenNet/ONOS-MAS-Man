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

    private ArrayList<ControllerBean> controllers;
    private ArrayList<PMBean> pms;
    private HashMap<PMBean, ArrayList<ControllerBean>> relationships;


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
