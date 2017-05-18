package kr.postech.monet.config.bean;

/**
 * Created by woojoong on 2017-05-18.
 */
public class DBBean {

    private String dbAlias;
    private String dbIPAddress;
    private String dbAdminPort;
    private String dbQueryPort;

    private String accessDBIPAddress;
    private String accessDBAdminPort;
    private String accessDBQueryPort;

    public DBBean(String dbAlias, String dbIPAddress, String dbAdminPort, String dbQueryPort, String accessDBIPAddress, String accessDBAdminPort, String accessDBQueryPort) {
        this.dbAlias = dbAlias;
        this.dbIPAddress = dbIPAddress;
        this.dbAdminPort = dbAdminPort;
        this.dbQueryPort = dbQueryPort;
        this.accessDBIPAddress = accessDBIPAddress;
        this.accessDBAdminPort = accessDBAdminPort;
        this.accessDBQueryPort = accessDBQueryPort;
    }

    public String getDbAlias() {
        return dbAlias;
    }

    public void setDbAlias(String dbAlias) {
        this.dbAlias = dbAlias;
    }

    public String getDbIPAddress() {
        return dbIPAddress;
    }

    public void setDbIPAddress(String dbIPAddress) {
        this.dbIPAddress = dbIPAddress;
    }

    public String getDbAdminPort() {
        return dbAdminPort;
    }

    public void setDbAdminPort(String dbAdminPort) {
        this.dbAdminPort = dbAdminPort;
    }

    public String getDbQueryPort() {
        return dbQueryPort;
    }

    public void setDbQueryPort(String dbQueryPort) {
        this.dbQueryPort = dbQueryPort;
    }

    public String getAccessDBIPAddress() {
        return accessDBIPAddress;
    }

    public void setAccessDBIPAddress(String accessDBIPAddress) {
        this.accessDBIPAddress = accessDBIPAddress;
    }

    public String getAccessDBAdminPort() {
        return accessDBAdminPort;
    }

    public void setAccessDBAdminPort(String accessDBAdminPort) {
        this.accessDBAdminPort = accessDBAdminPort;
    }

    public String getAccessDBQueryPort() {
        return accessDBQueryPort;
    }

    public void setAccessDBQueryPort(String accessDBQueryPort) {
        this.accessDBQueryPort = accessDBQueryPort;
    }
}
