package kr.postech.monet.core.database.bean;

/**
 * Created by woojoong on 2017-05-18.
 */
public class CPULoadBean {
    private String time;
    private float cpuLoad;

    public CPULoadBean(String time, float cpuLoad) {
        this.time = time;
        this.cpuLoad = cpuLoad;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(float cpuLoad) {
        this.cpuLoad = cpuLoad;
    }
}
