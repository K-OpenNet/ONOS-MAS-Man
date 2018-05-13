package Mastership;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Utils.Connection.RESTConnection;
import Utils.Parser.JsonParser;
import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

import static Database.Configure.Configuration.RESTURL_CHECKMASTERSHIP;
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
        ThreadChangeSingleMastership runnableObj = new ThreadChangeSingleMastership(dpid, targetController.getControllerId());
        Thread thread = new Thread(runnableObj);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        ArrayList<Thread> threads = new ArrayList<>();
        for (String dpid : switches) {
            ThreadChangeSingleMastership runnableObj = new ThreadChangeSingleMastership(controllerId, dpid);
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

class ThreadChangeSingleMastership implements Runnable {
    private String controllerId;
    private String dpid;

    public ThreadChangeSingleMastership(String controllerId, String dpid) {
        this.controllerId = controllerId;
        this.dpid = dpid;
    }

    @Override
    public void run() {

        changeMastership(dpid, controllerId);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int index = 0; index < 20; index++) {
            if (!verifyMastership(dpid, controllerId)) {
                changeMastership(dpid, controllerId);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (index == 19) {
                System.out.println("Fail to change mastership");
            }
        }
    }

    public void changeMastership(String dpid, String controllerId) {
        ControllerBean tmpControllerBean = Configuration.getInstance().getControllerBeanWithId(controllerId);
        JsonObject rootObj = new JsonObject();
        rootObj.add("deviceId", dpid);
        rootObj.add("nodeId", tmpControllerBean.getControllerId());
        rootObj.add("role", "MASTER");

        RESTConnection restConn = new RESTConnection();
        restConn.putCommandToUser(tmpControllerBean, RESTURL_DOMASTERSHIP, rootObj);
    }

    public boolean verifyMastership(String dpid, String controllerId) {
        ControllerBean tmpControllerBean = Configuration.getInstance().getControllerBeanWithId(controllerId);
        String url = RESTURL_CHECKMASTERSHIP.replace("<deviceID>", dpid);
        RESTConnection restConn = new RESTConnection();
        String tmpResult = restConn.sendCommandToUser(tmpControllerBean, url);
        JsonParser parser = new JsonParser();
        String result = null;
        for (int index = 0; index < 5; index++) {
            // for retry
            try {
                result = parser.parseMasterController(tmpResult);
                if (controllerId.equals(result)) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                System.out.println("********");
                System.out.println(url);
                System.out.println(controllerId);
                System.out.println("retry index: " + index);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return false;

        //System.out.println(controllerId + " -> " + result);

    }
}
