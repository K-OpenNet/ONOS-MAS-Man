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

        ArrayList<Thread> threads = new ArrayList<>();

        for (String controllerId : topology.keySet()) {
            ThreadChangeMultipleMastership runnableObj = new ThreadChangeMultipleMastership(controllerId, topology.get(controllerId));
            Thread thread = new Thread(runnableObj);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class ThreadChangeMultipleMastership implements Runnable {

    private String controllerId;
    private ArrayList<String> switches;

    public ThreadChangeMultipleMastership(String controllerId, ArrayList<String> switches) {
        this.controllerId = controllerId;
        this.switches = switches;
    }

    @Override
    public void run() {
        ControllerBean tmpControllerBean = Configuration.getInstance().getControllerBeanWithId(controllerId);
        for (String dpid : switches) {
            changeMastership(dpid, tmpControllerBean);
        }
    }

    public void changeMastership(String dpid, ControllerBean targetController) {
        JsonObject rootObj = new JsonObject();
        rootObj.add("deviceId", dpid);
        rootObj.add("nodeId", targetController.getControllerId());
        rootObj.add("role", "MASTER");

        RESTConnection restConn = new RESTConnection();
        restConn.putCommandToUser(targetController, RESTURL_DOMASTERSHIP, rootObj);
    }

}
