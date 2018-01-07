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

        String cmd = "cat ";

        // CPU #0 is always online.
        for (int index = 1; index <= controllerBean.getMaxCPUs(); index++) {
            cmd += CMD_CPU_BITMAP_TEMPLATE.replace("<index>", String.valueOf(index));

            cmd += " ";
        }

        String[] results = sshConn.sendCommandToRoot(controllerBean, cmd).split("\n");

        // Sanity check between MaxCPU in ControllerBean and Controller VM's information
        if (controllerBean.getMaxCPUs() != (results.length + 1)) {
            System.out.println(controllerBean.getBeanKey() + ": MaxCPU is different from Controller VM's CPU information (" +
                                       String.valueOf(controllerBean.getMaxCPUs()) +
                                       " != " + String.valueOf(results.length + 1));
            throw new CPUBitMapSanityException();
        }

        // CPU #0 is always online.
        controllerBean.getCpuBitmap()[0] = 1;
        for (int index = 1; index <= results.length; index++) {
            controllerBean.getCpuBitmap()[index] = Integer.valueOf(results[index - 1]);
        }

        return controllerBean.getCpuBitmap();
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
