package Monitor;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tuples.MastershipTuple;
import Utils.Connection.RESTConnection;
import Utils.Parser.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;

import static Database.Configure.Configuration.RESTURL_GETMASTERSHIPINFO;

public class MastershipMonitor extends AbstractMonitor implements Monitor {
    public MastershipMonitor() {
        monitorName = monitorType.MASTERSHIPMONITOR;
    }

    public String monitorRawMastership(ControllerBean controller) {

        RESTConnection restConn = new RESTConnection();

        return restConn.sendCommandToUser(controller, RESTURL_GETMASTERSHIPINFO);
    }

    public HashMap<String, MastershipTuple> monitorMastership() {

        JsonParser parser = new JsonParser();
        HashMap<String, MastershipTuple> results = new HashMap<>();
        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<ThreadGetMonitoringResultForMastership> rawResults = new ArrayList<>();

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {

            if (results.containsKey(controller.getBeanKey())) {
                System.out.println(controller.getBeanKey() + " is duplicated in results array. see MastershipMonitor.");
                throw new MastershipSanityException();
            }

            // Get switches which are serviced by this controller as raw data
            ThreadGetMonitoringResultForMastership tmpRunnableObj = new ThreadGetMonitoringResultForMastership(controller);
            Thread tmpThread = new Thread(tmpRunnableObj);
            rawResults.add(tmpRunnableObj);
            threads.add(tmpThread);
            tmpThread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (ThreadGetMonitoringResultForMastership runnableObj : rawResults) {
            results.put(runnableObj.getController().getBeanKey(), parser.parseMastershipMonitoringResults(runnableObj.getResult()));
        }

        return results;
    }

    private class ThreadGetMonitoringResultForMastership implements Runnable {

        private ControllerBean controller;
        private String result;

        public ThreadGetMonitoringResultForMastership(ControllerBean controller) {
            this.controller = controller;
        }

        @Override
        public void run() {
            this.result = monitorRawMastership(controller);
        }

        public ControllerBean getController() {
            return controller;
        }

        public void setController(ControllerBean controller) {
            this.controller = controller;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}

class MastershipSanityException extends RuntimeException {
    public MastershipSanityException() {
        super();
    }

    public MastershipSanityException(String message) {
        super(message);
    }
}

