package Monitor;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tuples.ControlPlaneTuple;
import Utils.Connection.RESTConnection;
import Utils.Parser.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;

import static Database.Configure.Configuration.RESTURL_CPMESSAGES;

public class ControlPlaneMonitor extends AbstractMonitor implements Monitor {
    public ControlPlaneMonitor() {
        monitorName = monitorType.CONTROLPLANEMONITOR;
    }

    public String monitorRawControlPlane(ControllerBean controller) {
        RESTConnection restConn = new RESTConnection();

        return restConn.sendCommandToUser(controller, RESTURL_CPMESSAGES);
    }

    public HashMap<String, HashMap<String, ControlPlaneTuple>> monitorControlPlane() {
        HashMap<String, HashMap<String, ControlPlaneTuple>> results = new HashMap<>();

        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<ThreadGetMonitoringResultForControlPlane> rawResults = new ArrayList<>();

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {

            if (!controller.isOnosAlive()) {
                continue;
            }

            if (results.containsKey(controller.getBeanKey())) {
                throw new ControlPlaneMonitoringSanityException();
            }

            ThreadGetMonitoringResultForControlPlane tmpRunnableObj = new ThreadGetMonitoringResultForControlPlane(controller);
            Thread tmpThread = new Thread(tmpRunnableObj);
            rawResults.add(tmpRunnableObj);
            threads.add(tmpThread);
            tmpThread.start();

        }

        for (Thread thread : threads) {
            try {
                thread.join(Configuration.MONITORING_PERIOD * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (ThreadGetMonitoringResultForControlPlane tmpRawResult : rawResults) {
            results.put(tmpRawResult.getController().getBeanKey(), tmpRawResult.getResult());
        }

        return results;
    }

    public HashMap<String, ControlPlaneTuple> monitorControlPlaneForEachController(ControllerBean controller) {
        String tmpRawResults = monitorRawControlPlane(controller);

        JsonParser parser = new JsonParser();

        return parser.parseControlPlaneMonitoringResult(controller, tmpRawResults);
    }

    private class ThreadGetMonitoringResultForControlPlane implements Runnable {

        private ControllerBean controller;
        private HashMap<String, ControlPlaneTuple> result;

        public ThreadGetMonitoringResultForControlPlane(ControllerBean controller) {
            this.controller = controller;
            this.result = new HashMap<>();
        }

        @Override
        public void run() {
            result = monitorControlPlaneForEachController(controller);
        }

        public ControllerBean getController() {
            return controller;
        }

        public void setController(ControllerBean controller) {
            this.controller = controller;
        }

        public HashMap<String, ControlPlaneTuple> getResult() {
            return result;
        }

        public void setResult(HashMap<String, ControlPlaneTuple> result) {
            this.result = result;
        }
    }

}

class ControlPlaneMonitoringSanityException extends RuntimeException {
    public ControlPlaneMonitoringSanityException() {
        super();
    }

    public ControlPlaneMonitoringSanityException(String message) {
        super(message);
    }
}
