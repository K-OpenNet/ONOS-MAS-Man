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
import static Database.Configure.Configuration.SSH_COMMAND_RETRIES;

public class ComputingResourceMonitor extends AbstractMonitor implements Monitor {


    public ComputingResourceMonitor() {
        monitorName = monitorType.COMPUTINGRESOURCEMONITOR;
    }


    public int[] monitorCPUBitMap (ControllerBean controllerBean) {

        if (!controllerBean.isVmAlive()) {
            return null;
        }

        ThreadGetNumCPUs runnableObj = new ThreadGetNumCPUs(controllerBean);
        Thread thread = new Thread(runnableObj);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        controllerBean.setCpuBitmap(runnableObj.getResults());

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

}

class ThreadGetNumCPUs implements Runnable {

    private ControllerBean controller;
    private int[] results;

    public ThreadGetNumCPUs(ControllerBean controller) {
        this.controller = controller;
        // Initialize CPU bitmap before monitoring
        for (int index = 0; index < controller.getCpuBitmap().length; index++) {
            controller.getCpuBitmap()[index] = 0;
        }
        this.results = controller.getCpuBitmap();
    }

    @Override
    public void run() {

        SSHParser parser = new SSHParser();

        String rawResults = getCPUBitmapRawMonitoringResult(controller);
        results = parser.parseCPUBitmap(rawResults, results);
    }

    public String getCPUBitmapRawMonitoringResult (ControllerBean controller) {

        SSHConnection sshConn = new SSHConnection();

        String results = "";

        int index = 0;

        while (results.equals("") || results == null) {

            if (index > SSH_COMMAND_RETRIES) {
                System.out.println("SSH Retry exception occurs");
                throw new SSHRetryExceedException();
            }

            results = sshConn.sendCommandToRoot(controller, CMD_CPU_BITMAP_TEMPLATE);

            index++;
        }

        return results;

    }

    public int[] getResults() {
        return results;
    }

    public ControllerBean getController() {
        return controller;
    }
}

class ThreadGetMonitoringResultForComputingResource implements Runnable {

    private String results;
    private PMBean pm;

    public ThreadGetMonitoringResultForComputingResource(PMBean pm) {
        this.pm = pm;
    }

    @Override
    public void run() {
        results = monitorRawComputingResource(pm);
    }

    public String monitorRawComputingResource (PMBean pm) {

        SSHConnection sshConn = new SSHConnection();

        return sshConn.sendCommandToUser(pm, CMD_COMPUTING_RESOURCE_QUERY);

    }

    public String getResults() {
        return results;
    }

    public PMBean getPm() {
        return pm;
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
