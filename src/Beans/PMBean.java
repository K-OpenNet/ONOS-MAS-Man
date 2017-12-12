package Beans;

import com.jcraft.jsch.Session;

public class PMBean extends AbstractBean implements Bean {
    private String ipAddr;
    private String sshPort;
    private String sshId;
    private String sshPw;
    private String sshRootId;
    private String sshRootPw;
    private Session userSession;
    private Session rootSession;

    public PMBean(String ipAddr) {
        this.ipAddr = ipAddr;
        this.beanKey = ipAddr;
        this.beanName = beanType.PM;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getSshPort() {
        return sshPort;
    }

    public void setSshPort(String sshPort) {
        this.sshPort = sshPort;
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
}
