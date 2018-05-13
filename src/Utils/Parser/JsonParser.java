package Utils.Parser;

import Beans.ControllerBean;
import Beans.MininetBean;
import Beans.PMBean;
import Database.Configure.Configuration;
import Database.Tuples.ControlPlaneTuple;
import Database.Tuples.MastershipTuple;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.projectfloodlight.openflow.protocol.OFType;

import java.util.ArrayList;
import java.util.HashMap;

public class JsonParser extends AbstractParser implements Parser {
    public JsonParser() {
        parserName = parserType.JSON;
    }

    public void parseAndMakeConfiguration (String rawJsonString) {
        if (rawJsonString == null) {
            throw new NullPointerException();
        }

        Configuration config = Configuration.getInstance();

        JsonObject parser = JsonObject.readFrom(rawJsonString);
        JsonArray controllersArray = parser.get("Controllers").asArray();
        JsonArray pmsArray = parser.get("PMs").asArray();
        JsonArray relationshipArray = parser.get("Relationships").asArray();
        JsonArray mininetArray = parser.get("Mininet").asArray();

        // For Controllers
        for (int index = 0; index < controllersArray.size(); index++) {
            JsonObject tmpObj = controllersArray.get(index).asObject();

            ControllerBean tmpControllerBean = new ControllerBean(tmpObj.get("controllerId").asString());
            tmpControllerBean.setName(tmpObj.get("name").asString());
            tmpControllerBean.setIpAddr(tmpObj.get("ipAddr").asString());
            tmpControllerBean.setRestPort(tmpObj.get("restPort").asString());
            tmpControllerBean.setSshPort(tmpObj.get("sshPort").asString());
            tmpControllerBean.setControllerGuiId(tmpObj.get("controllerGuiId").asString());
            tmpControllerBean.setControllerGuiPw(tmpObj.get("controllerGuiPw").asString());
            tmpControllerBean.setSshId(tmpObj.get("sshId").asString());
            tmpControllerBean.setSshPw(tmpObj.get("sshPw").asString());
            tmpControllerBean.setSshRootId(tmpObj.get("sshRootId").asString());
            tmpControllerBean.setSshRootPw(tmpObj.get("sshRootPw").asString());
            tmpControllerBean.setMaxCPUs(Integer.valueOf(tmpObj.get("numMaxCPU").asString()));
            tmpControllerBean.setMinCPUs(Integer.valueOf(tmpObj.get("numMinCPU").asString()));
            tmpControllerBean.setNumCPUs(Integer.valueOf(tmpObj.get("numMaxCPU").asString()));

            int[] tmpControllerCPUBitMap = new int[tmpControllerBean.getMaxCPUs()];
            for (int index1 = 0; index1 < tmpControllerBean.getMaxCPUs(); index1++) {
                tmpControllerCPUBitMap[index1] = 0;
            }
            tmpControllerBean.setCpuBitmap(tmpControllerCPUBitMap);

            config.getControllers().add(tmpControllerBean);
        }

        // For PMs
        for (int index = 0; index < pmsArray.size(); index++) {
            JsonObject tmpObj = pmsArray.get(index).asObject();

            PMBean tmpPMBean = new PMBean(tmpObj.get("ipAddr").asString());
            tmpPMBean.setSshPort(tmpObj.get("sshPort").asString());
            tmpPMBean.setSshId(tmpObj.get("sshId").asString());
            tmpPMBean.setSshPw(tmpObj.get("sshPw").asString());
            tmpPMBean.setSshRootId(tmpObj.get("sshRootId").asString());
            tmpPMBean.setSshRootPw(tmpObj.get("sshRootPw").asString());

            config.getPms().add(tmpPMBean);
        }

        // For Relationships
        for (int index1 = 0; index1 < relationshipArray.size(); index1++) {
            JsonObject tmpObj = relationshipArray.get(index1).asObject();
            String tmpPmIpAddr = tmpObj.get("PMIpAddr").asString();

            PMBean tmpPMBean = config.getPMBean(tmpPmIpAddr);
            config.getRelationships().putIfAbsent(tmpPMBean, new ArrayList<>());

            JsonArray tmpControllers = tmpObj.get("Controllers").asArray();
            for (int index2 = 0; index2 < tmpControllers.size(); index2++) {
                JsonObject tmpControllerObj = tmpControllers.get(index2).asObject();
                String tmpControllerName = tmpControllerObj.get("name").asString();
                String tmpControllerId = tmpControllerObj.get("controllerId").asString();

                ControllerBean tmpControllerBean = config.getControllerBean(tmpControllerName);
                config.getRelationships().get(tmpPMBean).add(tmpControllerBean);
            }
        }

        // For Mininets
        for (int index1 = 0; index1 < mininetArray.size(); index1++) {
            JsonObject tmpObj = mininetArray.get(index1).asObject();
            String mininetIp = tmpObj.get("ipAddr").asString();
            String mininetSshId = tmpObj.get("sshId").asString();
            String mininetSshPw = tmpObj.get("sshPw").asString();
            String mininetRootId = tmpObj.get("sshRootId").asString();
            String mininetRootPw = tmpObj.get("sshRootPw").asString();
            config.getMininets().putIfAbsent(mininetIp, new ArrayList<>());

            JsonArray switches = tmpObj.get("switches").asArray();
            for (int index2 = 0; index2 < switches.size(); index2++) {
                String dpid = switches.get(index2).asObject().get("dpid").asString();
                String id = switches.get(index2).asObject().get("id").asString();
                config.getMininets().get(mininetIp).add(new MininetBean(dpid, id));
            }

            PMBean pm = new PMBean(mininetIp);
            pm.setSshPort("22");
            pm.setSshId(mininetSshId);
            pm.setSshPw(mininetSshPw);
            pm.setSshRootId(mininetRootId);
            pm.setSshRootPw(mininetRootPw);
            config.getMininetMachines().putIfAbsent(pm.getIpAddr(), pm);

        }
    }

    // Key: controllerId, value: DPID
    public HashMap<String, ArrayList<String>> parseInitialState (String rawJsonString) {

        if (rawJsonString == null) {
            throw new NullPointerException();
        }

        HashMap<String, ArrayList<String>> results = new HashMap<>();

        JsonObject parser = JsonObject.readFrom(rawJsonString);
        JsonArray controllersArray = parser.get("Controllers").asArray();

        for (int index1 = 0; index1 < controllersArray.size(); index1++) {
            ArrayList<String> tmpArray = new ArrayList<>();

            JsonObject tmpObj = controllersArray.get(index1).asObject();
            String tmpControllerId = tmpObj.get("controllerId").asString();

            if (results.containsKey(tmpControllerId)) {
                throw new InitialStateSanityException();
            }

            JsonArray tmpSwitches = tmpObj.get("switches").asArray();

            for (int index2 = 0; index2 < tmpSwitches.size(); index2++) {
                tmpArray.add(tmpSwitches.get(index2).asString());
            }

            results.put(tmpControllerId, tmpArray);
        }

        return results;
    }

    public MastershipTuple parseMastershipMonitoringResults(String rawResults) {
        MastershipTuple result = new MastershipTuple();

        JsonObject parser = JsonObject.readFrom(rawResults);
        JsonArray switches = parser.get("deviceIds").asArray();
        for (int index = 0; index < switches.size(); index++) {
            String tmpDpid = switches.get(index).asString();
            if (tmpDpid.contains("ovsdb")) {
                continue;
            }
            result.addSwitch(tmpDpid);
        }

        return result;
    }

    public HashMap<String, ControlPlaneTuple> parseControlPlaneMonitoringResult(ControllerBean controller, String rawResult) {
        HashMap<String, ControlPlaneTuple> results = new HashMap<>();

        if(rawResult == null || rawResult == "") {
            throw new NullPointerException();
        }

        JsonObject parser = JsonObject.readFrom(rawResult);
        JsonArray switches = parser.get("devices").asArray();

        for (int index = 0; index < switches.size(); index++) {
            JsonObject elemSwitch = switches.get(index).asObject();
            String tmpDpid = elemSwitch.get("id").asString();

            if (results.containsKey(tmpDpid)) {
                throw new ControlPlaneMonitoringSanityException();
            }

            results.put(tmpDpid, new ControlPlaneTuple());
            ControlPlaneTuple resultTuple = results.get(tmpDpid);

            resultTuple.setDpid(tmpDpid);
            for (OFType type : OFType.values()) {
                if (type.equals(OFType.CONTROLLER_STATUS)) {
                    resultTuple.getControlTrafficByteResults().put(type, (long) 0);
                    resultTuple.getControlTrafficResults().put(type, (long) 0);
                    continue;
                }
                resultTuple.getControlTrafficResults().put(type, Long.valueOf(elemSwitch.get(type.toString()).asString()));
                resultTuple.getControlTrafficByteResults().put(type, Long.valueOf(elemSwitch.get(type.toString()+"[bytes]").asString()));
            }
        }

        return results;
    }

    public String parseMasterController(String rawResult) {
        if(rawResult == null || rawResult == "") {
            throw new NullPointerException();
        }

        JsonObject parser = JsonObject.readFrom(rawResult);
        return parser.get("master").asString();
    }
}

class ControlPlaneMonitoringSanityException extends RuntimeException {
    public ControlPlaneMonitoringSanityException() {
        super();
    }

    public ControlPlaneMonitoringSanityException(String message) {
        super(message);
    }
}

class InitialStateSanityException extends RuntimeException {
    public InitialStateSanityException() { super();
    }

    public InitialStateSanityException(String message) {
        super(message);
    }
}
