package Scaling;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tables.State;
import Database.Tuples.ControlPlaneTuple;
import Database.Tuples.MastershipTuple;
import Mastership.CPManMastership;
import org.projectfloodlight.openflow.protocol.OFType;

import java.util.ArrayList;
import java.util.HashMap;

public class ControllerScaling extends AbstractScaling implements Scaling {

    public ControllerScaling() {
        scalingName = scalingType.CONTROLLERSCALING;
    }

    public void runL1ONOSScaleIn() {
    }

    public void runL2ONOSScaleIn() {
    }

    public void runL3ONOSScaleIn() {
    }

    public void runL1ONOSScaleOut() {
    }

    public void runL2ONOSScaleOut() {
    }

    public void runL3ONOSScaleOut() {
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
        targetController.setActive(false);
    }

    public void distributeMastershipForScaleOut(ControllerBean targetController, State state) {
        if (!targetController.isOnosAlive()) {
            throw new L1TargetControllerSanityException();
        }
        targetController.setActive(true);
        CPManMastership mastership = new CPManMastership();
        mastership.runMastershipAlgorithm(state);
    }

    public void switchOffControllerForScaleIn() {

    }

    public void switchOnControllerForScaleOut() {

    }

    public void switchOffVMForScaleIn() {

    }

    public void switchOnVMForScaleOut() {

    }
}

class L1TargetControllerSanityException extends RuntimeException {
    public L1TargetControllerSanityException() {
    }

    public L1TargetControllerSanityException(String message) {
        super(message);
    }
}
