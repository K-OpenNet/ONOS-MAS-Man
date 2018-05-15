package Mastership;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tuples.MastershipTuple;
import Monitor.MastershipMonitor;
import Utils.Connection.RESTConnection;
import Utils.Parser.JsonParser;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static Database.Configure.Configuration.RESTURL_CHECKMASTERSHIP;
import static Database.Configure.Configuration.RESTURL_DOMASTERSHIP;
import static Database.Configure.Configuration.RESTURL_DOMULTIPLEMASTERSHIP;

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

//        ArrayList<Thread> threads = new ArrayList<>();
//
//        for (String controllerId : topology.keySet()) {
//            ThreadChangeMultipleMastership runnableObj = new ThreadChangeMultipleMastership(controllerId, topology.get(controllerId));
//            Thread thread = new Thread(runnableObj);
//            threads.add(thread);
//            thread.start();
//        }
//
//        for (Thread thread : threads) {
//            try {
//                thread.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        ArrayList<Thread> threads = new ArrayList<>();

        for (String nodeId : topology.keySet()) {

            JsonObject root = new JsonObject();
            root.add("nodeId", nodeId);
            JsonArray dpids = new JsonArray();

            for (String dpid : topology.get(nodeId)) {
                dpids.add(dpid);
            }

            root.add("dpids", dpids);

            ThreadMultipleMastershipChangeNew runnableObj = new ThreadMultipleMastershipChangeNew(nodeId, root);
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

        for (int index = 0; index < 50; index++) {
            boolean tmpVerify = verifyMastership(dpid, controllerId);
            if (!tmpVerify) {
                changeMastership(dpid, controllerId);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                return;
            }
        }

        System.out.println("Fail to change mastership");
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

class ThreadMultipleMastershipChangeNew implements Runnable {

    private String nodeId;
    private JsonObject topology;
    private MastershipMonitor monitor;
    private JsonParser parser;


    public ThreadMultipleMastershipChangeNew(String nodeId, JsonObject topology) {
        this.nodeId = nodeId;
        this.topology = topology;
        monitor = new MastershipMonitor();
        parser = new JsonParser();
    }

    @Override
    public void run() {
        JsonArray topologyResults = topology.get("dpids").asArray();
        ControllerBean tmpControllerBean = Configuration.getInstance().getControllerBeanWithId(nodeId);
        RESTConnection restConn = new RESTConnection();
        restConn.putCommandToUser(tmpControllerBean, RESTURL_DOMULTIPLEMASTERSHIP, topology);
//
//        for (int retryIndex = 0; retryIndex < 5; retryIndex++) {
//            MastershipTuple mastershipResult = parser.parseMastershipMonitoringResults(monitor.monitorRawMastership(tmpControllerBean));
//
//            for (int index = 0; index < topologyResults.size(); index++) {
//                String dpid = topologyResults.get(index).asString();
//
//                if (!mastershipResult.getSwitchList().contains(dpid)) {
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    if (retryIndex == 4) {
//                        System.out.println("Failed to change mastership");
//                    }
//
//                    break;
//                }
//
//            }
//
//            break;
//
//        }

    }

}
