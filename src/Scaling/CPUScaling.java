package Scaling;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Utils.Connection.SSHConnection;

import static Scaling.Scaling.scalingType.CPUSCALING;

public class CPUScaling extends AbstractScaling implements Scaling {
    public CPUScaling() {
        scalingName = CPUSCALING;
    }

    public void incMultipleVirtualCPUs (int numCPUs, ControllerBean controller) {
        for (int index = 0; index < numCPUs; index++) {
            incVirtualCPU(controller);
        }
    }

    public void decMultipleVirtaulCPUs (int numCPUs, ControllerBean controller) {
        for (int index = 0; index < numCPUs; index++) {
            decVirtualCPU(controller);
        }
    }

    public void incVirtualCPU (ControllerBean controller) {
        int index = getCPUIndexForInc(controller);
        if (index != -1) {
            enableSpecificVirtualCPU(controller, index);
        }
    }

    public void decVirtualCPU (ControllerBean controller) {
        int index = getCPUIndexForDec(controller);
        if (index != -1) {
            disableSpecificVirtualCPU(controller, index);
        }
    }

    public void enableSpecificVirtualCPU (ControllerBean controller, int cpuNumber) {
        SSHConnection sshConn = new SSHConnection();
        sshConn.sendCommandToRoot(controller, Configuration.CMD_ENABLE_CPU.replace("<index>", String.valueOf(cpuNumber)));
    }

    public void disableSpecificVirtualCPU (ControllerBean controller, int cpuNumber) {
        SSHConnection sshConn = new SSHConnection();
        sshConn.sendCommandToRoot(controller, Configuration.CMD_DISABLE_CPU.replace("<index>", String.valueOf(cpuNumber)));
    }

    public int getCPUIndexForInc (ControllerBean controller) {

        for (int index = 0; index < controller.getCpuBitmap().length; index++) {
            if (controller.getCpuBitmap()[index] == 0) {
                return index;
            }
        }

        return -1;
    }

    public int getCPUIndexForDec (ControllerBean controller) {

        for (int index = controller.getCpuBitmap().length - 1; index >= 0; index--) {
            if (controller.getCpuBitmap()[index] == 1) {
                return index;
            }
        }

        return -1;
    }
}
