package Controller;

import Beans.ControllerBean;
import Beans.PMBean;
import Database.Configure.Configuration;
import Monitor.ComputingResourceMonitor;
import UserInterface.CLI.CommandLine;
import Utils.Connection.SSHConnection;
import Utils.FileIO.FileIOUtil;
import Utils.Parser.JsonParser;

import java.util.ArrayList;

public class Controller {
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