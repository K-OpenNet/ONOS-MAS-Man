package kr.postech.monet.config.bean;

import kr.postech.monet.config.pool.VMConfPool;

import java.util.List;

/**
 * Created by woojoong on 2017-05-18.
 */
public class PMBean {

    private String pmAlias;
    private VMConfPool vmConfPool;

    private String ipAddress;
    private String sshPort;

    private String accessIPAddress;
    private String accessSSHPort;

    private String ID;
    private String PW;

    private int numCPU;

    private boolean alive;

    public PMBean(String pmAlias, List<VMBean> vmBeans) {
        this.pmAlias = pmAlias;
        this.vmConfPool = new VMConfPool();
        this.vmConfPool.setVmBeans(vmBeans);
        alive = false;
    }

    public String getPmAlias() {
        return pmAlias;
    }

    public void setPmAlias(String pmAlias) {
        this.pmAlias = pmAlias;
    }

    public VMConfPool getVmConfPool() {
        return vmConfPool;
    }

    public void setVmConfPool(VMConfPool vmConfPool) {
        this.vmConfPool = vmConfPool;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSshPort() {
        return sshPort;
    }

    public void setSshPort(String sshPort) {
        this.sshPort = sshPort;
    }

    public String getAccessIPAddress() {
        return accessIPAddress;
    }

    public void setAccessIPAddress(String accessIPAddress) {
        this.accessIPAddress = accessIPAddress;
    }

    public String getAccessSSHPort() {
        return accessSSHPort;
    }

    public void setAccessSSHPort(String accessSSHPort) {
        this.accessSSHPort = accessSSHPort;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getPW() {
        return PW;
    }

    public void setPW(String PW) {
        this.PW = PW;
    }

    public int getNumCPU() {
        return numCPU;
    }

    public void setNumCPU(int numCPU) {
        this.numCPU = numCPU;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
