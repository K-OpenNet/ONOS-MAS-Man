package UserInterface.CLI;

import Beans.PMBean;
import Controller.Controller;
import Database.Configure.Configuration;
import Database.Tables.Database;
import Database.Tables.State;
import DecisionMaker.DecisionMaker;
import Utils.Connection.SSHConnection;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import static Database.Configure.Configuration.*;

public class CommandLine {

    private Controller controller = Controller.getInstance();

    public void startCLI () {
        System.out.println("Start experiment");
        printMenuMessage();

        boolean flag_end = false;

        while (!flag_end) {
            Scanner sc = new Scanner(System.in);
            System.out.print("Insert Menu: ");

            int selectedItem = 0;
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
                    Controller.changeMasterControllerWithInitalState();
                    break;
                case 2:
                    // Make config to use Controller instance
                    printExperimentalParams();
                    System.out.print("Are you want to change experimental parameters? (y/n) ");
                    Scanner tmpSc = new Scanner(System.in);
                    char tmpInput = tmpSc.next().charAt(0);
                    if (tmpInput == 'y') {
                        changeExperimentalParams();
                    } else if (tmpInput == 'n') {

                    } else {
                        System.out.println("Wrong command, please try again.");
                    }
                    break;
                case 3:
                    // Run
                    runAlgorithm();
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
                case 10:
                    // equalizing Mastership
                    Controller.equalizingMastership();
                    break;
                case 11:
                    // manually change master controller
                    changeMasterControllerManually();
                    break;
                case 12:
                    // make initialstate.json file
                    break;
                case 13:
                    mergeMultipleStates();
                    break;
                case 14:
                    // run CPMan mastership function, onetime
                    runCPManMastershipFunction();
                    break;
                case 15:
                    // L1 scale-in, onetime
                    runL1ONOSScaleInFunction();
                    break;
                case 16:
                    // L2 scale-in, onetime
                    runL2ONOSScaleInFunction();
                    break;
                case 17:
                    // L2 scale-in, onetime
                    runL3ONOSScaleInFunction();
                    break;
                case 18:
                    // L1 scale-out, onetime
                    runL1ONOSScaleOutFunction();
                    break;
                case 19:
                    // L2 scale-out, onetime
                    runL2ONOSScaleOutFunction();
                    break;
                case 20:
                    runL3ONOSScaleOutFunction();
                    // L3 scale-out, onetime
                    break;
                case 21:
                    // get controllers
                    getControllers();
                    break;
                case 22:
                    // set controllers
                    setController();
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
        System.out.println("8: Increase vCPU of first ONOS controller -- for test");
        System.out.println("9: Decrease vCPU of first ONOS controller -- for test");
        System.out.println("10: Equalize Mastership");
        System.out.println("11: Manually change master controller for specified switch");
        System.out.println("12: Make initialstate.json file according to current state");
        System.out.println("13: Test to merge multiple states");
        System.out.println("14: Run CPMan mastership function, onetime");
        System.out.println("15: Run L1 ONOS scale-in function, onetime");
        System.out.println("16: Run L2 ONOS scale-in function, onetime");
        System.out.println("17: Run L3 ONOS scale-in function, onetime");
        System.out.println("18: Run L1 ONOS scale-out function, onetime");
        System.out.println("19: Run L2 ONOS scale-out function, onetime");
        System.out.println("20: Run L3 ONOS scale-out function, onetime");
        System.out.println("21: Get controllers with given dpid");
        System.out.println("22: Set controllers with given dpid and controllers");
    }

    public void printConfigMessage () {
        // scaling method, mastership, scaling level, initial # controllers [1, 3], initial # cores/controller [1, 3], Emulation time
    }

    public void testFuncForDev () {
        PMBean pm = Configuration.getInstance().getPMBean(DEV_MACHINE_IP_ADDR);
        SSHConnection sshConn = new SSHConnection();
        System.out.println(sshConn.sendCommandToUser(pm, "onos-secure-ssh 192.168.200.101 | grep cpmanrt | awk \'{print $5}\'"));
    }

    public void runL1ONOSScaleInFunction() {
        Controller.runL1ONOSScaleInFunction();
    }

    public void runL2ONOSScaleInFunction() {
        Controller.runL2ONOSScaleInFunction();
    }

    public void runL3ONOSScaleInFunction() {
        Controller.runL3ONOSScaleInFunction();
    }

    public void runL1ONOSScaleOutFunction() {
        Controller.runL1ONOSScaleOutFunction();
    }

    public void runL2ONOSScaleOutFunction() {
        Controller.runL2ONOSScaleOutFunction();
    }

    public void runL3ONOSScaleOutFunction() {
        Controller.runL3ONOSScaleOutFunction();
    }

    public void runCPManMastershipFunction() {
        Controller.runCPManMastershipFunction();
    }

    public void getControllers() {
        Scanner sc1 = new Scanner(System.in);
        System.out.println("DPID: ");
        String input1 = sc1.nextLine();
        String id = Configuration.getInstance().getId(input1);
        String mininetIp = Configuration.getInstance().getIpWithDpid(input1);
        SSHConnection sshConn = new SSHConnection();
        PMBean pm = Configuration.getInstance().getMininetMachines().get(mininetIp);
        String cmd = CMD_GET_CONTROLLER.replace("<switchID>", id);
        System.out.println(cmd); //debugging
        String result = sshConn.sendCommandToRoot(pm, cmd);
        System.out.println(result);

    }

    public void setController() {
        Scanner sc1 = new Scanner(System.in);
        System.out.println("DPID: ");
        String input1 = sc1.nextLine();
        String id = Configuration.getInstance().getId(input1);
        String mininetIp = Configuration.getInstance().getIpWithDpid(input1);
        System.out.println("Controllers: ");
        input1 = sc1.nextLine();
        String cmd = CMD_SET_CONTROLLER.replace("<switchID>", id).replace("<controllerIDs>", input1);
        PMBean pm = Configuration.getInstance().getMininetMachines().get(mininetIp);
        SSHConnection sshConn = new SSHConnection();
        System.out.println(cmd); //debugging
        sshConn.sendCommandToRoot(pm, cmd);
    }

    public void mergeMultipleStates() {
        int numMergeStates = 3;
        int finishedTimeIndex = Database.getDatabase().size() - 1;
        ArrayList<State> states = new ArrayList<>();
        for (int index = 0; index < numMergeStates ; index++) {
            State state = Database.getDatabase().get(finishedTimeIndex - index);
            states.add(state);
        }

        State state = Controller.mergeStates(states);

        System.out.println(Database.getInstance().getSingleTuple(state));
    }

    public void runMonitoringOnly() {
        DECISIONMAKER_TYPE = DecisionMaker.decisionMakerType.NOSCALING;
        Controller.runMonitoring();
    }

    public void runAlgorithm() {Controller.runMonitoring();}

    public void saveDBResult() {
        Controller.saveMonitoringResult(Database.getInstance().getOverallTuples());
    }

    public void changeMasterControllerManually () {
        Scanner sc1 = new Scanner(System.in);
        System.out.print("DPID: ");
        String input1 = "of:";
        input1 = sc1.nextLine();
        System.out.print("Controller ID: ");
        String input2 = sc1.nextLine();

        Controller.changeMasterController(input1, input2);
    }

    public void printExperimentalParams() {
        System.out.println("*** current variables ***");
        System.out.println("The type of Decision maker algorithm: " + DECISIONMAKER_TYPE);
        System.out.println("Monitoring period: " + MONITORING_PERIOD);
        System.out.println("Noscaling CPMan period: " + NOSCALING_CPMAN_PERIOD);
        System.out.println("Scaling level: " + SCALING_LEVEL);
        System.out.println("Scaling upper threshold: " + SCALING_THRESHOLD_UPPER);
        System.out.println("Scaling lower threshold: " + SCALING_THRESHOLD_LOWER);
        System.out.println("Minimum number of controllers: " + MIN_NUM_CONTROLLERS);
        System.out.println("Number of Mininet machines: " + NUM_MININET_MACHINE);
    }

    public void changeExperimentalParams() {
        System.out.println("*** change variables ***");
        Scanner sc1 = new Scanner(System.in);
        System.out.println("Decision Maker Algorithm");
        System.out.println("0: No Scaling");
        System.out.println("1: DCORAL");
        System.out.println("2: Scaling considering with CPU");
        System.out.println("3: Scaling considering with Networking");
        System.out.println("4: No Scaling with CPMan");
        System.out.print("Choose one of algorithms: ");
        int algorithm = sc1.nextInt();
        switch(algorithm) {
            case 0:
                DECISIONMAKER_TYPE = DecisionMaker.decisionMakerType.NOSCALING;
                break;
            case 1:
                DECISIONMAKER_TYPE = DecisionMaker.decisionMakerType.DCORAL;
                break;
            case 2:
                DECISIONMAKER_TYPE = DecisionMaker.decisionMakerType.SCALING_CPU;
                break;
            case 3:
                DECISIONMAKER_TYPE = DecisionMaker.decisionMakerType.SCALING_NETWORK;
                break;
            case 4:
                DECISIONMAKER_TYPE = DecisionMaker.decisionMakerType.NOSCALING_CPMAN;
                break;
            default:
                DECISIONMAKER_TYPE = DecisionMaker.decisionMakerType.NOSCALING_CPMAN;
                break;
        }
        System.out.print("Monitoring period: ");
        MONITORING_PERIOD = sc1.nextInt();
        System.out.print("Noscaling CPMan period: ");
        NOSCALING_CPMAN_PERIOD = sc1.nextInt();
        System.out.print("Scaling level: ");
        SCALING_LEVEL = sc1.nextInt();
        System.out.print("Scaling upper threshold: ");
        SCALING_THRESHOLD_UPPER = sc1.nextInt();
        System.out.print("Scaling lower threshold: ");
        SCALING_THRESHOLD_LOWER = sc1.nextInt();
        System.out.print("Minimum number of controllers: ");
        MIN_NUM_CONTROLLERS = sc1.nextInt();
        System.out.print("Number of Mininet machines: ");
        NUM_MININET_MACHINE = sc1.nextInt();
    }
}
