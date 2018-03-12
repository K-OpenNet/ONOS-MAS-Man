package Controller;

import Beans.ControllerBean;
import Beans.PMBean;
import Database.Configure.Configuration;
import Database.Tables.Database;
import Database.Tables.State;
import Database.Tuples.ComputingResourceTuple;
import Database.Tuples.ControlPlaneTuple;
import Database.Tuples.MastershipTuple;
import DecisionMaker.*;
import Mastership.EqualizingMastership;
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
import java.util.concurrent.locks.ReentrantLock;

import static Database.Configure.Configuration.FILE_NAME_PREFIX;
import static Database.Configure.Configuration.MONITORING_PERIOD;

public class Controller {

    private static int timeIndex;
    public static ReentrantLock lock = new ReentrantLock();

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

    // try to run DecisionMaker
    public static void runDecisionMaker(int timeIndex, DecisionMaker.decisionMakerType decisionMakerName, ArrayList<State> dbDump) {

        ThreadDecisionMaker threadDecisionMakerObj = new ThreadDecisionMaker(timeIndex, decisionMakerName, dbDump);
        Thread thread = new Thread(threadDecisionMakerObj);
        thread.run();
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
        NoScalingEqualizingAlgorithm noscaling = new NoScalingEqualizingAlgorithm();
        noscaling.runDecisionMakerAlgorithm(timeIndex, new ArrayList<>());
    }

    // Set initial state in accordance with InitialState.json
    public static void changeMasterControllerWithInitalState() {
        FileIOUtil fileIOUtil = new FileIOUtil();
        JsonParser jsonParser = new JsonParser();
        EqualizingMastership mastership = new EqualizingMastership();
        mastership.changeMultipleMastership(jsonParser.parseInitialState(fileIOUtil.getRawFileContents("initialstate.json")));
    }

    // Change master controller manually
    public static void changeMasterController(String dpid, String controllerId) {
        ControllerBean tmpController = Configuration.getInstance().getControllerBeanWithId(controllerId);
        EqualizingMastership mastership = new EqualizingMastership();
        mastership.changeMastership(dpid, tmpController);
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

class ThreadDecisionMaker implements Runnable {

    private int currentTimeIndex;
    private DecisionMaker.decisionMakerType algorithmType;
    private ArrayList<State> dbDump;

    public ThreadDecisionMaker(int currentTimeIndex, DecisionMaker.decisionMakerType algorithmType, ArrayList<State> dbDump) {
        this.dbDump = dbDump;
        this.currentTimeIndex = currentTimeIndex;
        this.algorithmType = algorithmType;
    }

    @Override
    public void run() {
        if (Controller.lock.isLocked()) {
            System.out.println("*** Decision maker does not finished yet (timeslot: " + currentTimeIndex + ")");
            return;
        }

        Controller.lock.lock();

        Date dt = new Date();
        long startTime = dt.getTime();

        DecisionMaker dmAlgorithm;

        switch(algorithmType) {
            case DCORAL:
                dmAlgorithm = new DCORALAlgorithm();
                break;
            case SCALING_CPU:
                dmAlgorithm = new CPUScalingAlgorithm();
                break;
            case SCALING_NETWORK:
                dmAlgorithm = new NetworkingScalingAlgorithm();
                break;
            case NOSCALING_CPMAN:
                dmAlgorithm = new NoScalingCPManAlgorithm();
                break;
            case NOSCALING:
                dmAlgorithm = new NoScalingEqualizingAlgorithm();
                break;
            default:
                dmAlgorithm = new NoScalingEqualizingAlgorithm();
                break;
        }

        dmAlgorithm.runDecisionMakerAlgorithm(currentTimeIndex, dbDump);

        dt = new Date();
        System.out.println("** Scaling time: " + (dt.getTime() - startTime));

        Controller.lock.unlock();
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

            // ToDo: run decision maker algorithm function
            ArrayList<State> dbDump = (ArrayList<State>) Database.getDatabase().clone();
            Controller.runDecisionMaker(Controller.getTimeIndex(), Configuration.DECISIONMAKER_TYPE, dbDump);

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