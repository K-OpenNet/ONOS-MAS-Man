package kr.postech.monet.core.parser;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import kr.postech.monet.config.bean.SWBean;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by woojoong on 2017-05-18.
 */
public class RESTParser {
    public RESTParser() {
    }

    public List<SWBean> parseGetSwitches(String jsonRaw) {
        List<SWBean> parsingResults = new CopyOnWriteArrayList<SWBean>();

        if(jsonRaw == null) {
            return new CopyOnWriteArrayList<SWBean>();
        }

        JsonObject parser = JsonObject.readFrom(jsonRaw);
        JsonArray switchObjs = parser.get("devices").asArray();
        for (int index = 0; index < switchObjs.size(); index++) {
            JsonObject tmpSwitchObj = switchObjs.get(index).asObject();
            String tmpSwitchID = tmpSwitchObj.get("id").asString();
            SWBean tmpSwitch = new SWBean(tmpSwitchID);
            parsingResults.add(tmpSwitch);
        }

        return parsingResults;
    }

    public List<String> parseGetMasterRoleSwitches(String jsonRaw) {
        List<String> parsingResults = new CopyOnWriteArrayList<String>();

        if(jsonRaw == null) {
            return new CopyOnWriteArrayList<String>();
        }

        JsonObject parser = JsonObject.readFrom(jsonRaw);
        JsonArray switchObjs = parser.get("deviceIds").asArray();
        for (int index = 0; index < switchObjs.size(); index++) {
            String tmpSwitchID = switchObjs.get(index).asString();
            parsingResults.add(tmpSwitchID);
        }
        return parsingResults;
    }

    public List<SWBean> parseGetCPTraffic(String jsonRaw, List<SWBean> sourceSWes) {

        //DPID, SWBean for random access
        HashMap<String, SWBean> tmpHashMap = new HashMap<String, SWBean>();

        if (jsonRaw == null) {
            return new CopyOnWriteArrayList<SWBean>();
        }

        JsonObject parser = JsonObject.readFrom(jsonRaw);
        JsonArray rawCPTrafficObjs = parser.get("devices").asArray();

        // parsing raw data
        for (int index = 0; index < rawCPTrafficObjs.size(); index++) {
            JsonObject switchCPTrafficObj = rawCPTrafficObjs.get(index).asObject();
            String tmpSwitchID = switchCPTrafficObj.get("name").asString();
            JsonArray switchCPTrafficMetricObj = switchCPTrafficObj.get("value").asObject().get("metrics").asArray();
            SWBean tmpSwitch = new SWBean(tmpSwitchID);
            tmpSwitch.setInboundPackets(switchCPTrafficMetricObj.get(0).asObject().
                    get("inboundPacket").asObject().get("latest").asInt());
            tmpSwitch.setOutboundPackets(switchCPTrafficMetricObj.get(1).asObject().
                    get("outboundPacket").asObject().get("latest").asInt());
            tmpSwitch.setFlowModPackets(switchCPTrafficMetricObj.get(2).asObject().
                    get("flowModPacket").asObject().get("latest").asInt());
            tmpSwitch.setFlowRemovePackets(switchCPTrafficMetricObj.get(3).asObject().
                    get("flowRemovedPacket").asObject().get("latest").asInt());
            tmpSwitch.setStatRequestPackets(switchCPTrafficMetricObj.get(4).asObject().
                    get("requestPacket").asObject().get("latest").asInt());
            tmpSwitch.setStatReplyPackets(switchCPTrafficMetricObj.get(5).asObject().
                    get("replyPacket").asObject().get("latest").asInt());

            tmpHashMap.put(tmpSwitchID, tmpSwitch);
        }

        // Mapping raw data to given List<SWBean>
        for (int index = 0; index < sourceSWes.size(); index++) {
            SWBean tmpSWBean = sourceSWes.get(index);
            SWBean tmpResultSWBean = tmpHashMap.get(tmpSWBean.getDpid());
            tmpSWBean.setInboundPackets(tmpResultSWBean.getInboundPackets());
            tmpSWBean.setOutboundPackets(tmpResultSWBean.getOutboundPackets());
            tmpSWBean.setFlowModPackets(tmpResultSWBean.getFlowModPackets());
            tmpSWBean.setFlowRemovePackets(tmpResultSWBean.getFlowRemovePackets());
            tmpSWBean.setStatRequestPackets(tmpResultSWBean.getStatRequestPackets());
            tmpSWBean.setStatReplyPackets(tmpResultSWBean.getStatReplyPackets());
        }

        tmpHashMap.clear();
        return sourceSWes;
    }

}
