package Controller;

import Beans.ControllerBean;
import Beans.PMBean;
import Database.Configure.Configuration;
import UserInterface.CLI.CommandLine;
import Utils.Connection.SSHConnection;
import Utils.FileIO.FileIOUtil;
import Utils.Parser.JsonParser;

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
        SSHConnection conn = new SSHConnection();
        // assign SSH session for each PM
        for (PMBean pm : config.getPms()) {
            conn.assignUserSession(pm);
            conn.assignRootSession(pm);

            System.out.println("The SSH sessions for the PM, " + pm.getBeanKey() + ", has been set");

        }

        // assign SSH session for each Controller
        for (ControllerBean controller : config.getControllers()) {
            conn.assignUserSession(controller);
            conn.assignRootSession(controller);

            System.out.println("The SSH sessions for the Controller, " + controller.getBeanKey() + ", has been set");
        }

        // ToDo: make CPU bitmap for controller

        System.out.println("** Initialization has been finished.");
    }

    public static void main (String[] args) {
        CommandLine cli = new CommandLine();
        cli.startCLI();
    }

}
