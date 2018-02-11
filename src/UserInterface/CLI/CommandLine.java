package UserInterface.CLI;

import Controller.Controller;
import Database.Configure.Configuration;
import Database.Tables.Database;
import Database.Tables.State;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class CommandLine {

    private Controller controller = Controller.getInstance();

    public void startCLI () {
        System.out.println("Start experiment");
        printMenuMessage();

        boolean flag_end = false;

        while (!flag_end) {
            Scanner sc = new Scanner(System.in);
            System.out.print("Insert Menu: ");

            int selectedItem = -1;
            try {
                selectedItem = sc.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Wrong input value");
            }

            System.out.println();

            switch (selectedItem) {
                case 0:
                    printMenuMessage();
                    break;
                case 1:
                    // Go to initial state
                    break;
                case 2:
                    // Make config to use Controller instance
                    break;
                case 3:
                    // Run
                    break;
                case 4:
                    // Monitoring only
                    runMonitoringOnly();
                    break;
                case 5:
                    // Save monitoring results to file
                    saveDBResult();
                    break;
                case 6:
                    // test
                    testFuncForDev();
                    break;
                case 7:
                    System.out.println(Database.getInstance().getOverallTuples());
                    break;
                case 8:
                    // inc vCPU
                    Controller.incVirtualCPUs(1, Configuration.getInstance().getControllers().get(0));
                    break;
                case 9:
                    // dec vCPU
                    Controller.decVirtualCPUs(1, Configuration.getInstance().getControllers().get(0));
                    break;
                default:
                    flag_end = true;
                    break;
            }
            //print results in total
        }
    }

    public void printMenuMessage () {
        System.out.println("Help");
        System.out.println("1: Make initial state");
        System.out.println("2: Change configuration parameters");
        System.out.println("3: Run Algorithm");
        System.out.println("4: Run monitoring only");
        System.out.println("5: Dump database");
        System.out.println("6: Reserved function - for test");
        System.out.println("7: Print database");
        System.out.println("8: Increase vCPU of first ONOS controller");
        System.out.println("9: Decrease vCPU of first ONOS controller");
    }

    public void printConfigMessage () {
        // scaling method, mastership, scaling level, initial # controllers [1, 3], initial # cores/controller [1, 3], Emulation time
    }

    public void testFuncForDev () {

    }

    public void runMonitoringOnly() {
        Controller.runMonitoring();
    }

    public void saveDBResult() {
        Controller.saveMonitoringResult(Database.getInstance().getOverallTuples());
    }
}
