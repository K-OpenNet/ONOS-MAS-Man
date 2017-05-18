package kr.postech.monet.core.parser;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import kr.postech.monet.config.bean.SWBean;
import kr.postech.monet.core.database.bean.CPULoadBean;
import kr.postech.monet.core.database.bean.ControlTrafficBean;
import kr.postech.monet.core.database.bean.NumSwitchesBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
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

    public List<ControlTrafficBean> parseDBControlTraffic(String jsonRaw) {
        List<ControlTrafficBean> resultList = new CopyOnWriteArrayList<ControlTrafficBean>();
        if (jsonRaw == null) {
            return new CopyOnWriteArrayList<ControlTrafficBean>();
        }

        StringReader sr = new StringReader(jsonRaw);
        BufferedReader br = new BufferedReader(sr);

        try {
            jsonRaw = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject parser = JsonObject.readFrom(jsonRaw);
        JsonArray rawCPTrafficInDB = parser.get("results").asArray();
        JsonObject tableCPTrafficInDBObjs = rawCPTrafficInDB.get(0).asObject();
        JsonArray tableCPTrafficInDB = tableCPTrafficInDBObjs.get("series").asArray();
        JsonObject contentsCPTrafficInDBObjs = tableCPTrafficInDB.get(0).asObject();
        JsonArray contentsCPTrafficInDB = contentsCPTrafficInDBObjs.get("values").asArray();
        for (int index = 0; index < contentsCPTrafficInDB.size(); index++) {
            JsonArray elementCPTrafficInDB = contentsCPTrafficInDB.get(index).asArray();
            ControlTrafficBean tmpResultControlTrafficBean = new ControlTrafficBean(elementCPTrafficInDB.get(0).asString(),
                    Integer.valueOf(elementCPTrafficInDB.get(1).asString()),
                    Integer.valueOf(elementCPTrafficInDB.get(2).asString()),
                    Integer.valueOf(elementCPTrafficInDB.get(3).asString()),
                    Integer.valueOf(elementCPTrafficInDB.get(4).asString()),
                    Integer.valueOf(elementCPTrafficInDB.get(5).asString()),
                    Integer.valueOf(elementCPTrafficInDB.get(6).asString()),
                    elementCPTrafficInDB.get(7).asInt());
            resultList.add(tmpResultControlTrafficBean);
        }


        return resultList;
    }

    public List<CPULoadBean> parseDBCPULoad(String jsonRaw) {
        List<CPULoadBean> resultList = new CopyOnWriteArrayList<CPULoadBean>();

        if (jsonRaw == null) {
            return resultList;
        }

        StringReader sr = new StringReader(jsonRaw);
        BufferedReader br = new BufferedReader(sr);

        try {
            jsonRaw = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject parser = JsonObject.readFrom(jsonRaw);
        JsonArray rawCPULoadInDB = parser.get("results").asArray();
        JsonObject tableCPULoadInDBObjs = rawCPULoadInDB.get(0).asObject();
        JsonArray tableCPULoadInDB = tableCPULoadInDBObjs.get("series").asArray();
        JsonObject contentsCPULoadInDBObjs = tableCPULoadInDB.get(0).asObject();
        JsonArray contentsCPULoadInDB = contentsCPULoadInDBObjs.get("values").asArray();
        for (int index = 0; index < contentsCPULoadInDB.size(); index++) {
            JsonArray elementCPULoadInDB = contentsCPULoadInDB.get(index).asArray();

            CPULoadBean tmpCPULoadBean = new CPULoadBean(elementCPULoadInDB.get(0).asString(), elementCPULoadInDB.get(1).asFloat());
            resultList.add(tmpCPULoadBean);
        }


        return resultList;
    }

    public List<NumSwitchesBean> parseDBNumSwitches(String jsonRaw) {
        List<NumSwitchesBean> resultList = new CopyOnWriteArrayList<NumSwitchesBean>();

        if (jsonRaw == null) {
            return resultList;
        }

        StringReader sr = new StringReader(jsonRaw);
        BufferedReader br = new BufferedReader(sr);

        try {
            jsonRaw = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject parser = JsonObject.readFrom(jsonRaw);
        JsonArray rawNumSwitchesInDB = parser.get("results").asArray();
        JsonObject tableNumSwitchesInDBObjs = rawNumSwitchesInDB.get(0).asObject();
        JsonArray tableNumSwitchesInDB = tableNumSwitchesInDBObjs.get("series").asArray();
        JsonObject contentsNumSwitchesInDBObjs = tableNumSwitchesInDB.get(0).asObject();
        JsonArray contentsNumSwitchesInDB = contentsNumSwitchesInDBObjs.get("values").asArray();
        for (int index = 0; index < contentsNumSwitchesInDB.size(); index++) {
            JsonArray elementNumSwitches = contentsNumSwitchesInDB.get(index).asArray();

            NumSwitchesBean tmpNumSwitchesBean = new NumSwitchesBean(elementNumSwitches.get(0).asString(),
                    elementNumSwitches.get(1).asInt());
            resultList.add(tmpNumSwitchesBean);
        }

        return resultList;
    }
}
