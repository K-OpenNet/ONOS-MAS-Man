package Controller;

import UserInterface.CLI.CommandLine;
import org.projectfloodlight.openflow.protocol.OFType;

public class Controller {
    private static Controller ourInstance = new Controller();

    public static Controller getInstance() {
        return ourInstance;
    }

    private Controller() {
    }

    public static void main (String[] args) {
        CommandLine cli = new CommandLine();
        cli.startCLI();
    }
}
