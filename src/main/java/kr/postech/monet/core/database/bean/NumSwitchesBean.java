package kr.postech.monet.core.database.bean;

/**
 * Created by woojoong on 2017-05-18.
 */
public class NumSwitchesBean {
    private String time;
    private int numSwitches;

    public NumSwitchesBean(String time, int numSwitches) {
        this.time = time;
        this.numSwitches = numSwitches;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getNumSwitches() {
        return numSwitches;
    }

    public void setNumSwitches(int numSwitches) {
        this.numSwitches = numSwitches;
    }
}
