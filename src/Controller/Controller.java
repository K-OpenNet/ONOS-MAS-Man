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
import Mastership.CPManMastership;
import Mastership.EqualizingMastership;
import Monitor.ComputingResourceMonitor;
import Monitor.ControlPlaneMonitor;
import Monitor.MastershipMonitor;
import Scaling.ControllerScaling;
import UserInterface.CLI.CommandLine;
import Utils.Connection.SSHConnection;
import Utils.FileIO.FileIOUtil;
import Utils.Parser.JsonParser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

import static Database.Configure.Configuration.*;
import static Database.Configure.Configuration.FIXED_CONTROLLER_ID_3;

public class Controller {

    private static int timeIndex;
    public static ReentrantLock lock = new ReentrantLock();

    public static ReentrantLock hecpL1Lock = new ReentrantLock();
    public static ReentrantLock hecpL2Lock = new ReentrantLock();

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

        // assign SSH session for each Mininet
        for (String mininetIp : config.getMininetMachines().keySet()) {
            PMBean tmpPm = config.getMininetMachines().get(mininetIp);
            ThreadPMSSHSessionAssignment pmRunnableObj = new ThreadPMSSHSessionAssignment(tmpPm);
            Thread thread = new Thread(pmRunnableObj);
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

    public static void initEnv() {

        int numStandbyControllersBeingUsed = 0;
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            if (controller.getControllerId().equals(FIXED_CONTROLLER_ID_1) ||
                    controller.getControllerId().equals(FIXED_CONTROLLER_ID_2) ||
                    controller.getControllerId().equals(FIXED_CONTROLLER_ID_3)) {
                continue;
            }

            if (DECISIONMAKER_TYPE == DecisionMaker.decisionMakerType.HECP && numStandbyControllersBeingUsed < NUM_STANDBY_CONTROLLER) {
                numStandbyControllersBeingUsed++;
                controller.setActive(false);
                controller.setOnosAlive(true);
                controller.setVmAlive(true);
                continue;
            }

            // L1 is unnecessary --> init an experimental environment will deal with it
            State state = new State();
            ControllerScaling scaling = new ControllerScaling();

            switch (SCALING_LEVEL) {
                case 1:
                    controller.setActive(false);
                    controller.setOnosAlive(true);
                    controller.setVmAlive(true);
                    break;
                case 2:
                    controller.setActive(false);
                    controller.setOnosAlive(false);
                    controller.setVmAlive(true);
                    scaling.switchOffControllerForScaleIn(controller, state);
                    break;
                case 3:
                    controller.setActive(false);
                    controller.setOnosAlive(false);
                    controller.setVmAlive(false);
                    scaling.switchOffControllerForScaleIn(controller, state);
                    scaling.switchOffVMForScaleIn(controller, state);
                    break;
                default:
                    throw new InitException();
            }
        }

    }

    // run Monitoring
    public static void runMonitoring() {

        if (!FIN_INIT_ENV) {
            // init an experimental environment
            // function: changeMasterControllerWithInitalState
            // function: initEnv
            changeMasterControllerWithInitalState();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            initEnv();
            FIN_INIT_ENV = true;
        }

        System.out.print("Are you ready to experiment? (y/n): ");
        Scanner sc1 = new Scanner(System.in);
        char tmpInput = sc1.next().charAt(0);
        if (tmpInput != 'y') {
            return;
        }

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
        thread.start();
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

    // merge states
    public static State mergeStates(ArrayList<State> states) {
        NoScalingEqualizingAlgorithm noScaling = new NoScalingEqualizingAlgorithm();
        return noScaling.mergeStates(states);
    }

    // run CPMan mastership just onetime
    public static void runCPManMastershipFunction() {
        State state = Database.getDatabase().get(timeIndex);
        CPManMastership mastership = new CPManMastership();
        mastership.runMastershipAlgorithm(state);
    }

    public static void runL1ONOSScaleInFunction() {
        State state = Database.getDatabase().get(timeIndex);
        ControllerBean targetController = Configuration.getInstance().getControllers().get(0);
        ControllerScaling scaling = new ControllerScaling();
        scaling.runL1ONOSScaleIn(targetController, state);
    }

    public static void runL2ONOSScaleInFunction() {
        State state = Database.getDatabase().get(timeIndex);
        ControllerBean targetController = Configuration.getInstance().getControllers().get(0);
        ControllerScaling scaling = new ControllerScaling();
        scaling.runL2ONOSScaleIn(targetController, state);
    }

    public static void runL3ONOSScaleInFunction() {
        State state = Database.getDatabase().get(timeIndex);
        ControllerBean targetController = Configuration.getInstance().getControllers().get(0);
        ControllerScaling scaling = new ControllerScaling();
        scaling.runL3ONOSScaleIn(targetController, state);
    }

    public static void runL1ONOSScaleOutFunction() {
        State state = Database.getDatabase().get(timeIndex);
        ControllerBean targetController = Configuration.getInstance().getControllers().get(0);
        ControllerScaling scaling = new ControllerScaling();
        scaling.runL1ONOSScaleOut(targetController, state);
    }

    public static void runL2ONOSScaleOutFunction() {
        State state = Database.getDatabase().get(timeIndex);
        ControllerBean targetController = Configuration.getInstance().getControllers().get(0);
        ControllerScaling scaling = new ControllerScaling();
        scaling.runL2ONOSScaleOut(targetController, state);
    }

    public static void runL3ONOSScaleOutFunction() {
        State state = Database.getDatabase().get(timeIndex);
        ControllerBean targetController = Configuration.getInstance().getControllers().get(0);
        ControllerScaling scaling = new ControllerScaling();
        scaling.runL3ONOSScaleOut(targetController, state);
    }

    public static void runCPUBasedMasstership() {
        //debugging
        System.out.println("***" + timeIndex);
        State state = Database.getDatabase().get(timeIndex);
        CPUScalingAlgorithm scaling = new CPUScalingAlgorithm();
        scaling.runCPULoadMastershipAlgorithm(state);
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
            case SDCORAL:
                dmAlgorithm = new SDCORALAlgorithm();
                break;
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
            case HECP:
                dmAlgorithm = new HybridECP();
                break;
            default:
                dmAlgorithm = new NoScalingEqualizingAlgorithm();
                break;
        }

        dmAlgorithm.runDecisionMakerAlgorithm(currentTimeIndex, dbDump);

        dt = new Date();
        System.out.println("** Scaling time: " + (dt.getTime() - startTime) + " (timeslot: " + currentTimeIndex + ", Algorithm: " + algorithmType + ")");

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

            Date dt = new Date();
            long tmpPrevTime = dt.getTime();
            System.out.println("Time: " + Controller.getTimeIndex() + " (Current time: " + tmpPrevTime + ")");

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
            // add Active flags
            for (ControllerBean controller : Configuration.getInstance().getControllers()) {
                if (!controller.isVmAlive()) {
                    tmpState.getActiveFlags().put(controller.getControllerId(), "IA-MX");
                } else if (!controller.isOnosAlive()) {
                    tmpState.getActiveFlags().put(controller.getControllerId(), "IA-OX");
                } else if (!controller.isActive()) {
                    tmpState.getActiveFlags().put(controller.getControllerId(), "IA-OO");
                } else {
                    tmpState.getActiveFlags().put(controller.getControllerId(), "A");
                }
            }

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

class InitException extends RuntimeException {
    public InitException() {
    }

    public InitException(String message) {
        super(message);
    }
}