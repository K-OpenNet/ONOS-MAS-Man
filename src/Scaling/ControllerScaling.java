package Scaling;

import Beans.ControllerBean;
import Beans.PMBean;
import Database.Configure.Configuration;
import Database.Tables.State;
import Mastership.CPManMastership;
import Utils.Connection.RESTConnection;
import Utils.Connection.SSHConnection;

import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.HashMap;

import static Database.Configure.Configuration.*;

public class ControllerScaling extends AbstractScaling implements Scaling {

    public ControllerScaling() {
        scalingName = scalingType.CONTROLLERSCALING;
    }

    public void runL1ONOSScaleIn(ControllerBean targetController, State state) {
        distributeMastershipForScaleIn(targetController, state);
        targetController.setActive(false);
    }

    public void runL2ONOSScaleIn(ControllerBean targetController, State state) {
        runL1ONOSScaleIn(targetController, state);
        switchOffControllerForScaleIn(targetController, state);
        targetController.setOnosAlive(false);
    }

    public void runL3ONOSScaleIn(ControllerBean targetController, State state) {
        runL2ONOSScaleIn(targetController, state);
        switchOffVMForScaleIn(targetController, state);
        targetController.setVmAlive(false);
    }

    public void runL1ONOSScaleOut(ControllerBean targetController, State state) {
        targetController.setActive(true);
        distributeMastershipForScaleOut(targetController, state);
    }

    public void runL2ONOSScaleOut(ControllerBean targetController, State state) {
        targetController.setOnosAlive(true);
        switchOnControllerForScaleOut(targetController, state);
        runL1ONOSScaleOut(targetController, state);
    }

    public void runL3ONOSScaleOut(ControllerBean targetController, State state) {
        targetController.setVmAlive(true);
        switchOnVMForScaleOut(targetController, state);
        runL2ONOSScaleOut(targetController, state);
    }

    public void distributeMastershipForScaleIn(ControllerBean targetController, State state) {
        if (!targetController.isOnosAlive()) {
            throw new L1TargetControllerSanityException();
        }

        CPManMastership mastership = new CPManMastership();

        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();
        ArrayList<ControllerBean> otherControllers = mastership.getActiveControllers();
        otherControllers.remove(targetController);

        HashMap<String, ArrayList<String>> topology = new HashMap<>();
        for (ControllerBean controller : otherControllers) {
            topology.putIfAbsent(controller.getControllerId(), new ArrayList<>());
        }

        // get switches
        ArrayList<String> switchesInTargetController = mastership.getSortedSwitchList(targetController, state);

        // make estimated hashmap variable for other controllers
        HashMap<ControllerBean, Long> estimatedOtherControllerOFMsgs = new HashMap<>();
        for (ControllerBean controller : otherControllers) {
            long tmpNumOFMsgs = mastership.getNumOFMsgsForSingleController(controller, state);
            estimatedOtherControllerOFMsgs.putIfAbsent(controller, tmpNumOFMsgs);
        }

        // distribute from target to other active controllers
        for (String dpid : switchesInTargetController) {
            long tmpOFMsgs = mastership.getNumOFMsgsForSingleSwitchInMasterController(targetController, dpid, state);
            ControllerBean leastController = mastership.getLeastOFMsgsController(estimatedOtherControllerOFMsgs);
            long prevOFMsgs = estimatedOtherControllerOFMsgs.get(leastController);
            long changedOFMsgs = prevOFMsgs + tmpOFMsgs;
            estimatedOtherControllerOFMsgs.replace(leastController, changedOFMsgs);
            topology.get(leastController.getControllerId()).add(dpid);
        }

        mastership.changeMultipleMastership(topology);
    }

    public void distributeMastershipForScaleOut(ControllerBean targetController, State state) {
        if (!targetController.isOnosAlive()) {
            throw new L1TargetControllerSanityException();
        }
        CPManMastership mastership = new CPManMastership();
        mastership.runMastershipAlgorithm(state);
    }

    public void switchOffControllerForScaleIn(ControllerBean targetController, State state) {
        PMBean pm = Configuration.getInstance().getPMBean(DEV_MACHINE_IP_ADDR);

        RESTConnection restConn = new RESTConnection();
        SSHConnection sshConn = new SSHConnection();

        String url = RESTURL_DOSCALEIN.replace("<controllerID>", targetController.getControllerId());
        try {
            restConn.sendCommandToUser(targetController, url);
        } catch (BadRequestException e) {
            System.out.println("BadRequestException");
        }
        String serviceStopCMD = CMD_ONOS_SERVICE_STOP.replace("<controllerID>", targetController.getControllerId());
        sshConn.sendCommandToUser(pm, serviceStopCMD);
    }

    public void switchOnControllerForScaleOut(ControllerBean targetController, State state) {
        PMBean pm = Configuration.getInstance().getPMBean(DEV_MACHINE_IP_ADDR);

        RESTConnection restConn = new RESTConnection();
        SSHConnection sshConn = new SSHConnection();

        String url = RESTURL_DOSCALEOUT.replace("<controllerID>", targetController.getControllerId());
        try {
            restConn.sendCommandToUser(targetController, url);

        } catch (BadRequestException e) {
            System.out.println("BadRequestException");
        }
        String serviceStartCMD = CMD_ONOS_SERVICE_START.replace("<controllerID>", targetController.getControllerId());
        String checkServiceCMD = CMD_CHECK_ONOS_SERVICE.replace("<controllerID>", targetController.getControllerId());
        sshConn.sendCommandToUser(pm, serviceStartCMD);
        System.out.println(sshConn.sendCommandToUser(pm, checkServiceCMD));
    }

    public void switchOffVMForScaleIn(ControllerBean targetController, State state) {

    }

    public void switchOnVMForScaleOut(ControllerBean targetController, State state) {

    }
}

class L1TargetControllerSanityException extends RuntimeException {
    public L1TargetControllerSanityException() {
    }

    public L1TargetControllerSanityException(String message) {
        super(message);
    }
}
