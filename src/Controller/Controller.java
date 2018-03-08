package Controller;

import Beans.ControllerBean;
import Beans.PMBean;
import Database.Configure.Configuration;
import Database.Tables.Database;
import Database.Tables.State;
import Database.Tuples.ComputingResourceTuple;
import Database.Tuples.ControlPlaneTuple;
import Database.Tuples.MastershipTuple;
import DecisionMaker.DCORALAlgorithm;
import Monitor.ComputingResourceMonitor;
import Monitor.ControlPlaneMonitor;
import Monitor.MastershipMonitor;
import UserInterface.CLI.CommandLine;
import Utils.Connection.SSHConnection;
import Utils.FileIO.FileIOUtil;
import Utils.Parser.JsonParser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static Database.Configure.Configuration.FILE_NAME_PREFIX;
import static Database.Configure.Configuration.MONITORING_PERIOD;

public class Controller {

    private static int timeIndex;

    private static Controller ourInstance = new Controller();

    public static Controller getInstance() {
        return ourInstance;
    }

    private Controller() {
        init();
    }

    // Initialization function
    public static void init() {
        System.out.println("** Initialization now...");

        timeIndex = 0;

        ArrayList<Thread> threads = new ArrayList<>();

        FileIOUtil fileIO = new FileIOUtil();
        JsonParser parser = new JsonParser();
        try {
            parser.parseAndMakeConfiguration(fileIO.getRawFileContents("config.json"));
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.exit(0);
        }

        System.out.println("- configuration file has been applied on this system, successfully");

        Configuration config = Configuration.getInstance();
        // assign SSH session for each PM
        for (PMBean pm : config.getPms()) {
            ThreadPMSSHSessionAssignment pmRunnableObj = new ThreadPMSSHSessionAssignment(pm);
            Thread thread = new Thread(pmRunnableObj);
            threads.add(thread);
            thread.start();
        }

        // assign SSH session for each Controller as well as assign CPUBitMap for each Controller
        for (ControllerBean controller : config.getControllers()) {
            ThreadControllerSSHSessionAssignment controllerRunnableObj = new ThreadControllerSSHSessionAssignment(controller);
            Thread thread = new Thread(controllerRunnableObj);
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

        System.out.println("** Initialization has been finished.");
    }

    // run Monitoring
    public static void runMonitoring() {
        timeIndex = 0;

        ComputingResourceMonitor crMon = new ComputingResourceMonitor();
        MastershipMonitor masMon = new MastershipMonitor();
        ControlPlaneMonitor cpMon = new ControlPlaneMonitor();

        ThreadMonitoring monitoringThread = new ThreadMonitoring(crMon, masMon, cpMon);
        Thread thr = new Thread(monitoringThread);
        thr.start();
    }

    public static int getTimeIndex() {
        return timeIndex;
    }

    public static void setTimeIndex(int timeIndex) {
        Controller.timeIndex = timeIndex;
    }

    // inc/dec vCPUs for a controller
    public static void incVirtualCPUs(int numCPUs, ControllerBean controller) {
        DCORALAlgorithm dcoral = new DCORALAlgorithm();
        dcoral.incVirtualCPUs(numCPUs, controller);
    }

    public static void decVirtualCPUs(int numCPUs, ControllerBean controller) {
        DCORALAlgorithm dcoral = new DCORALAlgorithm();
        dcoral.decVirtualCPUs(numCPUs, controller);
    }

    // save monitoring results
    public static void saveMonitoringResult(String results) {
        FileIOUtil util = new FileIOUtil();
        String path = FILE_NAME_PREFIX.replace("<timeindex>", String.valueOf(timeIndex));
        util.saveResultsToFile(path, results);
    }

    // Equalizing mastership
    public static void equalizingMastership() {
        DCORALAlgorithm dcoral = new DCORALAlgorithm();
        dcoral.equalizingMastership();
    }

    // Set initial state in accordance with InitialState.json
    public static void changeMasterControllerWithInitalState() {
        FileIOUtil fileIOUtil = new FileIOUtil();

    }

    public static void main (String[] args) {
        CommandLine cli = new CommandLine();
        cli.startCLI();
    }
}

class ThreadPMSSHSessionAssignment implements Runnable {

    private PMBean pm;

    public ThreadPMSSHSessionAssignment(PMBean pm) {
        this.pm = pm;
    }

    @Override
    public void run() {
        SSHConnection conn = new SSHConnection();
        conn.assignUserSession(pm);
        conn.assignRootSession(pm);

        System.out.println("The SSH sessions for the PM, " + pm.getBeanKey() + ", has been set");

    }
}

class ThreadMonitoring implements Runnable {

    private ComputingResourceMonitor crMon;
    private MastershipMonitor masMon;
    private ControlPlaneMonitor cpMon;
    private ArrayList<Thread> threads;

    public ThreadMonitoring(ComputingResourceMonitor crMon, MastershipMonitor masMon, ControlPlaneMonitor cpMon) {
        this.threads = new ArrayList<>();
        this.crMon = crMon;
        this.masMon = masMon;
        this.cpMon = cpMon;
    }

    @Override
    public void run() {

        while (true) {

            System.out.println("Time: " + Controller.getTimeIndex());
            Date dt = new Date();
            long tmpPrevTime = dt.getTime();

            ThreadComputingResourceMonitoring crMonThread = new ThreadComputingResourceMonitoring(crMon);
            ThreadMastershipMonitoring masMonThread = new ThreadMastershipMonitoring(masMon);
            ThreadControlPlaneMonitoring cpMonThread = new ThreadControlPlaneMonitoring(cpMon);
            ThreadNumCPUsMonitoring cpuMonThread = new ThreadNumCPUsMonitoring(crMon);

            Thread thrCrMon = new Thread(crMonThread);
            thrCrMon.setName("CRMonThread");
            Thread thrMasMon = new Thread(masMonThread);
            thrMasMon.setName("MasMonThread");
            Thread thrCpMon = new Thread(cpMonThread);
            thrCpMon.setName("CPMonThread");
            Thread thrCpuMon = new Thread(cpuMonThread);
            thrCpuMon.setName("CPUMonThread");

            threads.add(thrCrMon);
            threads.add(thrMasMon);
            threads.add(thrCpMon);
            threads.add(thrCpuMon);

            for (Thread thr : threads) {
                thr.start();
            }

            for (Thread thr : threads) {
                try {
                    thr.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            threads.clear();
            threads = new ArrayList<>();

            State tmpState = new State();
            tmpState.setComputingResourceTuples(crMonThread.getComputingResourceTuples());
            tmpState.setMastershipTuples(masMonThread.getMastershipTuples());
            tmpState.setControlPlaneTuples(cpMonThread.getControlPlaneTuples());
            tmpState.setNumCPUsTuples(cpuMonThread.getNumCPUsTuples());
            Database.getDatabase().add(Controller.getTimeIndex(), tmpState);

            dt = new Date();

            long elapseTime = dt.getTime() - tmpPrevTime;
            long remainTime = (MONITORING_PERIOD * 1000) - elapseTime;

            if (remainTime > 0) {
                try {
                    Thread.sleep(remainTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Controller.setTimeIndex(Controller.getTimeIndex() + 1);
        }
    }
}

class ThreadComputingResourceMonitoring implements Runnable {

    private ComputingResourceMonitor crMon;
    private HashMap<String, ComputingResourceTuple> computingResourceTuples;

    public ThreadComputingResourceMonitoring(ComputingResourceMonitor crMon) {
        this.crMon = crMon;
    }

    @Override
    public void run() {
        computingResourceTuples = crMon.monitorComputingResource();
    }

    public HashMap<String, ComputingResourceTuple> getComputingResourceTuples() {
        return computingResourceTuples;
    }

    public void setComputingResourceTuples(HashMap<String, ComputingResourceTuple> computingResourceTuples) {
        this.computingResourceTuples = computingResourceTuples;
    }
}

class ThreadMastershipMonitoring implements Runnable {

    private MastershipMonitor masMon;
    private HashMap<String, MastershipTuple> mastershipTuples;

    public ThreadMastershipMonitoring(MastershipMonitor masMon) {
        this.masMon = masMon;
    }

    @Override
    public void run() {
        mastershipTuples = masMon.monitorMastership();
    }

    public HashMap<String, MastershipTuple> getMastershipTuples() {
        return mastershipTuples;
    }

    public void setMastershipTuples(HashMap<String, MastershipTuple> mastershipTuples) {
        this.mastershipTuples = mastershipTuples;
    }
}

class ThreadControlPlaneMonitoring implements Runnable {

    private ControlPlaneMonitor cpMon;
    private HashMap<String, HashMap<String, ControlPlaneTuple>> controlPlaneTuples;

    public ThreadControlPlaneMonitoring(ControlPlaneMonitor cpMon) {
        this.cpMon = cpMon;
    }

    @Override
    public void run() {
        controlPlaneTuples = cpMon.monitorControlPlane();
    }

    public HashMap<String, HashMap<String, ControlPlaneTuple>> getControlPlaneTuples() {
        return controlPlaneTuples;
    }

    public void setControlPlaneTuples(HashMap<String, HashMap<String, ControlPlaneTuple>> controlPlaneTuples) {
        this.controlPlaneTuples = controlPlaneTuples;
    }
}

class ThreadNumCPUsMonitoring implements Runnable {

    private ComputingResourceMonitor crMon;
    private HashMap<String, Integer> numCPUsTuples;

    public ThreadNumCPUsMonitoring(ComputingResourceMonitor crMon) {
        this.crMon = crMon;
    }

    @Override
    public void run() {
        numCPUsTuples = crMon.monitorNumCPUs();
    }

    public HashMap<String, Integer> getNumCPUsTuples() {
        return numCPUsTuples;
    }

    public void setNumCPUsTuples(HashMap<String, Integer> numCPUsTuples) {
        this.numCPUsTuples = numCPUsTuples;
    }
}

class ThreadControllerSSHSessionAssignment implements Runnable {

    private ControllerBean controller;

    public ThreadControllerSSHSessionAssignment(ControllerBean controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        SSHConnection conn = new SSHConnection();
        ComputingResourceMonitor mon = new ComputingResourceMonitor();

        conn.assignUserSession(controller);
        conn.assignRootSession(controller);

        System.out.println("The SSH sessions for the Controller, " + controller.getBeanKey() + ", has been set");
        mon.monitorCPUBitMap(controller);
    }
}