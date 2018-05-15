package Beans;

import Database.Configure.Configuration;
import DecisionMaker.DecisionMaker;
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
    private boolean active;
    private boolean onosAlive;
    private boolean vmAlive;

    public ControllerBean(String controllerId) {
        this.controllerId = controllerId;
        this.beanKey = controllerId;
        this.beanName = beanType.CONTROLLER;
        this.active = true;
        this.onosAlive = true;
        this.vmAlive = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getRestPort() {
        return restPort;
    }

    public void setRestPort(String restPort) {
        this.restPort = restPort;
    }

    public String getSshPort() {
        return sshPort;
    }

    public void setSshPort(String sshPort) {
        this.sshPort = sshPort;
    }

    public String getControllerGuiId() {
        return controllerGuiId;
    }

    public void setControllerGuiId(String controllerGuiId) {
        this.controllerGuiId = controllerGuiId;
    }

    public String getControllerGuiPw() {
        return controllerGuiPw;
    }

    public void setControllerGuiPw(String controllerGuiPw) {
        this.controllerGuiPw = controllerGuiPw;
    }

    public String getSshId() {
        return sshId;
    }

    public void setSshId(String sshId) {
        this.sshId = sshId;
    }

    public String getSshPw() {
        return sshPw;
    }

    public void setSshPw(String sshPw) {
        this.sshPw = sshPw;
    }

    public String getSshRootId() {
        return sshRootId;
    }

    public void setSshRootId(String sshRootId) {
        this.sshRootId = sshRootId;
    }

    public String getSshRootPw() {
        return sshRootPw;
    }

    public void setSshRootPw(String sshRootPw) {
        this.sshRootPw = sshRootPw;
    }

    public Session getUserSession() {
        return userSession;
    }

    public void setUserSession(Session userSession) {
        this.userSession = userSession;
    }

    public Session getRootSession() {
        return rootSession;
    }

    public void setRootSession(Session rootSession) {
        this.rootSession = rootSession;
    }

    public int getLastTimeIndexChangeCPU() {
        return lastTimeIndexChangeCPU;
    }

    public void setLastTimeIndexChangeCPU(int lastTimeIndexChangeCPU) {
        this.lastTimeIndexChangeCPU = lastTimeIndexChangeCPU;
    }

    public int getNumCPUs() {

        if (Configuration.DECISIONMAKER_TYPE.equals(DecisionMaker.decisionMakerType.DCORAL) ||
                Configuration.DECISIONMAKER_TYPE.equals(DecisionMaker.decisionMakerType.SDCORAL)) {

            int minCPUs = 0;

            for (int index = 0; index < cpuBitmap.length; index++) {

                if (cpuBitmap[index] == 1) {
                    minCPUs++;
                }

            }

            return minCPUs;

        } else {
            return getMinCPUs();
        }


    }

    public void setNumCPUs(int numCPUs) {
        this.numCPUs = numCPUs;
    }

    public int getMaxCPUs() {
        return maxCPUs;
    }

    public void setMaxCPUs(int maxCPUs) {
        this.maxCPUs = maxCPUs;
    }

    public int getMinCPUs() {
        return minCPUs;
    }

    public void setMinCPUs(int minCPUs) {
        this.minCPUs = minCPUs;
    }

    public int[] getCpuBitmap() {
        return cpuBitmap;
    }

    public void setCpuBitmap(int[] cpuBitmap) {
        this.cpuBitmap = cpuBitmap;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isOnosAlive() {
        return onosAlive;
    }

    public void setOnosAlive(boolean onosAlive) {
        this.onosAlive = onosAlive;
    }

    public boolean isVmAlive() {
        return vmAlive;
    }

    public void setVmAlive(boolean vmAlive) {
        this.vmAlive = vmAlive;
    }
}
