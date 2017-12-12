package Beans;

public class SwitchBean extends AbstractBean implements Bean {
    private String dpid;

    public SwitchBean(String dpid) {
        this.dpid = dpid;
        this.beanKey = dpid;
        this.beanName = beanType.SWITCH;
    }
}
