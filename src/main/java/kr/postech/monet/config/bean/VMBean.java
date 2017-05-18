package kr.postech.monet.config.bean;

/**
 * Created by woojoong on 2017-05-18.
 */
public class VMBean {

    private String vmAlias;
    private String ipAddress;
    private String sshPort;
    private String httpPort;

    private String accessIPAddress;
    private String accessSSHPort;
    private String accessHTTPPort;

    private String ID;
    private String PW;

    private String ONOSID;
    private String ONOSPW;

    private boolean alive;

    private boolean aliveONOS;

    public VMBean(String vmAlias) {
        this.vmAlias = vmAlias;
        alive = false;
        aliveONOS = true;
    }

    public String getVmAlias() {
        return vmAlias;
    }

    public void setVmAlias(String vmAlias) {
        this.vmAlias = vmAlias;
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

    public String getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
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

    public String getAccessHTTPPort() {
        return accessHTTPPort;
    }

    public void setAccessHTTPPort(String accessHTTPPort) {
        this.accessHTTPPort = accessHTTPPort;
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

    public String getONOSID() {
        return ONOSID;
    }

    public void setONOSID(String ONOSID) {
        this.ONOSID = ONOSID;
    }

    public String getONOSPW() {
        return ONOSPW;
    }

    public void setONOSPW(String ONOSPW) {
        this.ONOSPW = ONOSPW;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isAliveONOS() {
        return aliveONOS;
    }

    public void setAliveONOS(boolean aliveONOS) {
        this.aliveONOS = aliveONOS;
    }
}
