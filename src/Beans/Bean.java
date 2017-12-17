package Beans;

import com.jcraft.jsch.Session;

public interface Bean {
    enum beanType {
        PM, CONTROLLER, SWITCH;
    }

    beanType getBeanName();

    String getBeanKey();

    String getIpAddr();

    String getSshPort();

    String getSshId();

    String getSshPw();

    String getSshRootId();

    String getSshRootPw();

    Session getUserSession();

    void setUserSession(Session userSession);

    Session getRootSession();

    void setRootSession(Session rootSession);
}
