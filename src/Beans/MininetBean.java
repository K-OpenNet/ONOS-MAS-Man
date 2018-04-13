package Beans;

public class MininetBean {
    private String dpid;
    private String id;

    public MininetBean(String dpid, String id) {
        this.dpid = dpid;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDpid() {

        return dpid;
    }

    public void setDpid(String dpid) {
        this.dpid = dpid;
    }
}
