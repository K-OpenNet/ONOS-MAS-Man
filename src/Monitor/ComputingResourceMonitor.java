package Monitor;

import Beans.ControllerBean;
import Beans.PMBean;
import Database.Configure.Configuration;
import Database.Tuples.ComputingResourceTuple;
import Utils.Connection.SSHConnection;
import Utils.Parser.SSHParser;

import java.util.ArrayList;
import java.util.HashMap;

import static Database.Configure.Configuration.CMD_COMPUTING_RESOURCE_QUERY;
import static Database.Configure.Configuration.CMD_CPU_BITMAP_TEMPLATE;

public class ComputingResourceMonitor extends AbstractMonitor implements Monitor {


    public ComputingResourceMonitor() {
        monitorName = monitorType.COMPUTINGRESOURCEMONITOR;
    }

    public int[] monitorCPUBitMap (ControllerBean controllerBean) {

        // Initialize CPU bitmap before monitoring
        for (int index = 0; index < controllerBean.getCpuBitmap().length; index++) {
            controllerBean.getCpuBitmap()[index] = 0;
        }

        SSHConnection sshConn = new SSHConnection();

        if (controllerBean.getRootSession() == null) {
            System.out.println(controllerBean.getBeanKey() + ": SSH Root session is not set up. Please set up it first");
            throw new NullPointerException();
        } else if (controllerBean.getMaxCPUs() == 0) {
            System.out.println(controllerBean.getBeanKey() + ": Wrong configuration of maxCPUs");
            throw new NullPointerException();
        } else if (controllerBean.getCpuBitmap() == null) {
            controllerBean.setCpuBitmap(new int[controllerBean.getMaxCPUs()]);
        }

        String cmd  = CMD_CPU_BITMAP_TEMPLATE;

        String tmpResults = sshConn.sendCommandToRoot(controllerBean, cmd);
        System.out.println(controllerBean.getBeanKey() + ": " + tmpResults);
        String results = tmpResults.split("\\s+")[3];

        ArrayList<Integer> rawResults = parseCPUBitmapFromVM(results);

        for (int elemResult : rawResults) {
            controllerBean.getCpuBitmap()[elemResult] = 1;
        }

        return controllerBean.getCpuBitmap();
    }

    public ArrayList<Integer> parseCPUBitmapFromVM (String rawResult) {

        ArrayList<Integer> results = new ArrayList<>();

        String[] arrayResults = rawResult.split(",");

        for (String elemResult : arrayResults) {
            if (elemResult.contains("-")) {
                String[] tmpResults = elemResult.split("-");
                int start = Integer.valueOf(tmpResults[0]);
                int end = Integer.valueOf(tmpResults[1]);

                for (int index = start; index <= end; index++) {
                    results.add(index);
                }

            } else {
                results.add(Integer.valueOf(elemResult));
            }

        }

        return results;
    }

    public HashMap<String, Integer> monitorNumCPUs() {

        HashMap<String, Integer> results = new HashMap<>();
        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<ThreadGetNumCPUs> rawResults = new ArrayList<>();

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {

            if (!controller.isVmAlive()) {
                continue;
            }

            if (results.containsKey(controller.getBeanKey())) {
                System.out.println(controller.getBeanKey() + " is duplicated in results");
                throw new CPUBitMapSanityException();
            }

            ThreadGetNumCPUs tmpRunnableObj = new ThreadGetNumCPUs(controller);
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

        for (ThreadGetNumCPUs runnableObj : rawResults) {
            results.put(runnableObj.getController().getBeanKey(), getNumActiveBit(runnableObj.getResults()));
        }

        return results;
    }

    public int getNumActiveBit(int[] rawResults) {

        int result = 0;

        for (int index = 0; index < rawResults.length; index++) {
            if (rawResults[index] == 1) {
                result++;
            }
        }

        return result;

    }

    public String monitorRawComputingResource (PMBean pm) {

        SSHConnection sshConn = new SSHConnection();

        return sshConn.sendCommandToUser(pm, CMD_COMPUTING_RESOURCE_QUERY);

    }

    public HashMap<String, ComputingResourceTuple> monitorComputingResource () {

        SSHParser parser = new SSHParser();

        HashMap<String, ComputingResourceTuple> results = new HashMap<>();
        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<ThreadGetMonitoringResultForComputingResource> rawResults = new ArrayList<>();

        // Initialize
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {

            if(!controller.isVmAlive()) {
                continue;
            }

            ComputingResourceTuple tmpTuple = new ComputingResourceTuple();
            results.put(controller.getBeanKey(), tmpTuple);
        }

        for (PMBean pm : Configuration.getInstance().getPms()) {

            ThreadGetMonitoringResultForComputingResource tmpRunnableObj = new ThreadGetMonitoringResultForComputingResource(pm);
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

        for (ThreadGetMonitoringResultForComputingResource runnableObj : rawResults) {
            parser.parseComputingResourceMonitoringResults(runnableObj.getResults(), runnableObj.getPm(), results);
        }

        return results;

    }

    private class ThreadGetNumCPUs implements Runnable {

        private ControllerBean controller;
        private int[] results;

        public ThreadGetNumCPUs(ControllerBean controller) {
            this.controller = controller;
        }

        @Override
        public void run() {
            results = monitorCPUBitMap(controller);
        }

        public int[] getResults() {
            return results;
        }

        public ControllerBean getController() {
            return controller;
        }
    }

    private class ThreadGetMonitoringResultForComputingResource implements Runnable {

        private String results;
        private PMBean pm;

        public ThreadGetMonitoringResultForComputingResource(PMBean pm) {
            this.pm = pm;
        }

        @Override
        public void run() {
            results = monitorRawComputingResource(pm);
        }

        public String getResults() {
            return results;
        }

        public PMBean getPm() {
            return pm;
        }
    }
}

class CPUBitMapSanityException extends RuntimeException {
    public CPUBitMapSanityException() {
        super();
    }

    public CPUBitMapSanityException(String message) {
        super(message);
    }
}
