package Monitor;

import Beans.ControllerBean;
import Utils.Connection.SSHConnection;

import static Database.Configure.Configuration.CMD_CPU_BITMAP_TEMPLATE;

public class ComputingResourceMonitor extends AbstractMonitor implements Monitor {

    private SSHConnection sshConn;

    public ComputingResourceMonitor() {
        monitorName = monitorType.COMPUTINGRESOURCEMONITOR;
        sshConn = new SSHConnection();
    }

    public int[] monitorCPUBitMap (ControllerBean controllerBean) {
        if (controllerBean.getRootSession() == null) {
            System.out.println(controllerBean.getBeanKey() + ": SSH Root session is not set up. Please set up it first");
            throw new NullPointerException();
        } else if (controllerBean.getMaxCPUs() == 0) {
            System.out.println(controllerBean.getBeanKey() + ": Wrong configuration of maxCPUs");
            throw new NullPointerException();
        } else if (controllerBean.getCpuBitmap() == null) {
            controllerBean.setCpuBitmap(new int[controllerBean.getMaxCPUs()]);
        }

        String cmd = "cat ";

        // CPU #0 is always online.
        for (int index = 1; index <= controllerBean.getMaxCPUs(); index++) {
            cmd += CMD_CPU_BITMAP_TEMPLATE.replace("<index>", String.valueOf(index));

            cmd += " ";
        }

        String[] results = sshConn.sendCommandToRoot(controllerBean, cmd).split("\n");

        // Sanity check between MaxCPU in ControllerBean and Controller VM's information
        if (controllerBean.getMaxCPUs() != (results.length + 1)) {
            System.out.println(controllerBean.getBeanKey() + ": MaxCPU is different from Controller VM's CPU information (" +
                                       String.valueOf(controllerBean.getMaxCPUs()) +
                                       " != " + String.valueOf(results.length + 1));
            throw new CPUBitMapSanityException();
        }

        // CPU #0 is always online.
        controllerBean.getCpuBitmap()[0] = 1;
        for (int index = 1; index <= results.length; index++) {
            controllerBean.getCpuBitmap()[index] = Integer.valueOf(results[index - 1]);
        }

        return controllerBean.getCpuBitmap();
    }
}

class CPUBitMapSanityException extends RuntimeException {
    public CPUBitMapSanityException() {
        super();
    }

    public CPUBitMapSanityException(String message) {
        super(message);
    }
}
