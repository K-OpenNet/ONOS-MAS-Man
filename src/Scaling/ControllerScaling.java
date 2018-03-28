package Scaling;

import Beans.ControllerBean;
import Database.Tables.State;

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
    }

    public void distributeMastershipForScaleOut(ControllerBean targetController, State state) {
        if (!targetController.isOnosAlive()) {
            throw new L1TargetControllerSanityException();
        }
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
