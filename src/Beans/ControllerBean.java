package Beans;

import com.jcraft.jsch.Session;

public class ControllerBean extends AbstractBean implements Bean {

    private String name;
    private String controllerId;
    private String ipAddr;
    private String restPort;
    private String sshPort;
    private String controllerGuiId;
    private String controllerGuiPw;
    private String sshId;
    private String sshPw;
    private String sshRootId;
    private String sshRootPw;
    private Session userSession;
    private Session rootSession;
    private int lastTimeIndexChangeCPU;
    private int numCPUs;
    private int maxCPUs;
    private int minCPUs;
    private int[] cpuBitmap;
    private boolean isActive;

    public ControllerBean(String controllerId) {
        this.controllerId = controllerId;
        this.minCPUs = 1;
        this.maxCPUs = 18;
        this.beanKey = controllerId;
        this.beanName = beanType.CONTROLLER;
        this.cpuBitmap = new int[18];
        this.isActive = false;
    }
}
