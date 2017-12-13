package UserInterface.CLI;

import Controller.Controller;

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
            int selectedItem = sc.nextInt();
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
                    // Reserve
                    break;
                case 5:
                    // Reserve
                    break;
                case 99:
                    // Print results
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
    }

    public void printConfigMessage () {
        // scaling method, mastership, scaling level, initial # controllers [1, 3], initial # cores/controller [1, 3], Emulation time
    }
}
