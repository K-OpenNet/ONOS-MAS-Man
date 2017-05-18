package kr.postech.monet.core.database.bean;

/**
 * Created by woojoong on 2017-05-18.
 */
public class ControlTrafficBean {
    private String time;
    private int numInbound;
    private int numOutbound;
    private int numFlowMod;
    private int numFlowRem;
    private int numStatReq;
    private int numStatRep;
    private int numTotalPackets;

    public ControlTrafficBean(String time, int numInbound, int numOutbound, int numFlowMod, int numFlowRem, int numStatReq, int numStatRep, int numTotalPackets) {
        this.time = time;
        this.numInbound = numInbound;
        this.numOutbound = numOutbound;
        this.numFlowMod = numFlowMod;
        this.numFlowRem = numFlowRem;
        this.numStatReq = numStatReq;
        this.numStatRep = numStatRep;
        this.numTotalPackets = numTotalPackets;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getNumInbound() {
        return numInbound;
    }

    public void setNumInbound(int numInbound) {
        this.numInbound = numInbound;
    }

    public int getNumOutbound() {
        return numOutbound;
    }

    public void setNumOutbound(int numOutbound) {
        this.numOutbound = numOutbound;
    }

    public int getNumFlowMod() {
        return numFlowMod;
    }

    public void setNumFlowMod(int numFlowMod) {
        this.numFlowMod = numFlowMod;
    }

    public int getNumFlowRem() {
        return numFlowRem;
    }

    public void setNumFlowRem(int numFlowRem) {
        this.numFlowRem = numFlowRem;
    }

    public int getNumStatReq() {
        return numStatReq;
    }

    public void setNumStatReq(int numStatReq) {
        this.numStatReq = numStatReq;
    }

    public int getNumStatRep() {
        return numStatRep;
    }

    public int getNumTotalPackets() {
        return numTotalPackets;
    }

    public void setNumTotalPackets(int numTotalPackets) {
        this.numTotalPackets = numTotalPackets;
    }

    public void setNumStatRep(int numStatRep) {
        this.numStatRep = numStatRep;
    }
}
