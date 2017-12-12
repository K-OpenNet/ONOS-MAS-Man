package UserInterface.CLI;

import java.util.Scanner;

public class CommandLine {

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
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                default:
                    flag_end = true;
                    break;
            }
        }
    }

    public void printMenuMessage () {
        System.out.println("Help");
    }
}
