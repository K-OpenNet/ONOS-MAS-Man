package kr.postech.monet.core.parser;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import kr.postech.monet.config.bean.SWBean;

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

}
