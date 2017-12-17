package Beans;

import com.jcraft.jsch.Session;

public class SwitchBean extends AbstractBean implements Bean {
    private String dpid;

    public SwitchBean(String dpid) {
        this.dpid = dpid;
        this.beanKey = dpid;
        this.beanName = beanType.SWITCH;
    }

    public String getDpid() {
        return dpid;
    }

    public void setDpid(String dpid) {
        this.dpid = dpid;
    }

    @Override
    public String getIpAddr() {
        return null;
    }

    @Override
    public String getSshPort() {
        return null;
    }

    @Override
    public String getSshId() {
        return null;
    }

    @Override
    public String getSshPw() {
        return null;
    }

    @Override
    public String getSshRootId() {
        return null;
    }

    @Override
    public String getSshRootPw() {
        return null;
    }

    @Override
    public Session getUserSession() {
        return null;
    }

    @Override
    public void setUserSession(Session userSession) {

    }

    @Override
    public Session getRootSession() {
        return null;
    }

    @Override
    public void setRootSession(Session rootSession) {

    }
}
