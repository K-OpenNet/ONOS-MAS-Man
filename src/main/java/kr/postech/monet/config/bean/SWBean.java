package kr.postech.monet.config.bean;

/**
 * Created by woojoong on 2017-05-18.
 */
public class SWBean {
    private String dpid;
    private int inboundPackets;
    private int outboundPackets;
    private int flowModPackets;
    private int flowRemovePackets;
    private int statRequestPackets;
    private int statReplyPackets;

    public SWBean(String dpid) {
        this.dpid = dpid;
    }

    public String getDpid() {
        return dpid;
    }

    public void setDpid(String dpid) {
        this.dpid = dpid;
    }

    public int getInboundPackets() {
        return inboundPackets;
    }

    public void setInboundPackets(int inboundPackets) {
        this.inboundPackets = inboundPackets;
    }

    public int getOutboundPackets() {
        return outboundPackets;
    }

    public void setOutboundPackets(int outboundPackets) {
        this.outboundPackets = outboundPackets;
    }

    public int getFlowModPackets() {
        return flowModPackets;
    }

    public void setFlowModPackets(int flowModPackets) {
        this.flowModPackets = flowModPackets;
    }

    public int getFlowRemovePackets() {
        return flowRemovePackets;
    }

    public void setFlowRemovePackets(int flowRemovePackets) {
        this.flowRemovePackets = flowRemovePackets;
    }

    public int getStatRequestPackets() {
        return statRequestPackets;
    }

    public void setStatRequestPackets(int statRequestPackets) {
        this.statRequestPackets = statRequestPackets;
    }

    public int getStatReplyPackets() {
        return statReplyPackets;
    }

    public void setStatReplyPackets(int statReplyPackets) {
        this.statReplyPackets = statReplyPackets;
    }

    public int getTotalControlPackets() {
        return inboundPackets + outboundPackets + flowModPackets + flowRemovePackets + statRequestPackets + statReplyPackets;
    }
}
