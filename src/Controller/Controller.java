package Controller;

import Database.Configure.Configuration;
import UserInterface.CLI.CommandLine;
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

    public static void init() {
        System.out.println("Initialization now...");

        FileIOUtil fileIO = new FileIOUtil();
        JsonParser parser = new JsonParser();
        try {
            parser.parseAndMakeConfiguration(fileIO.getRawFileContents("config.json"));
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.exit(0);
        }

        Configuration config = Configuration.getInstance();
        System.out.println("Initialization has been finished.");
    }

    public static void main (String[] args) {
        CommandLine cli = new CommandLine();
        cli.startCLI();
    }

}
