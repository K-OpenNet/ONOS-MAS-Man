package Mastership;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tables.State;
import Database.Tuples.ControlPlaneTuple;
import Database.Tuples.MastershipTuple;
import org.projectfloodlight.openflow.protocol.OFType;

import java.util.ArrayList;
import java.util.HashMap;

public class CPManMastership extends AbstractMastership implements Mastership {
    public CPManMastership() {
        mastershipName = mastershipType.CPMAN;
    }

    @Override
    public void runMastershipAlgorithm(State state) {
        HashMap<String, ArrayList<String>> topology = new HashMap<>();

        ArrayList<ControllerBean> activeControllers = getActiveControllers();
        ArrayList<ControllerBean> underSubControllers = getUnderSubControllers(activeControllers, state);
        ArrayList<ControllerBean> overSubControllers = getOverSubControllers(activeControllers, state);

        // initialize topology
        for (ControllerBean controller : activeControllers) {
            topology.putIfAbsent(controller.getControllerId(), new ArrayList<>());
        }

        // sort over-subscribed switch list
        HashMap<String, ArrayList<String>> sortedSwitchesUnderSub = new HashMap<>();
        for (ControllerBean controller : underSubControllers) {
            sortedSwitchesUnderSub.putIfAbsent(controller.getControllerId(), getSortedSwitchList(controller, state));
        }

        changeMultipleMastership(topology);
    }

    public ArrayList<ControllerBean> getActiveControllers() {
        ArrayList<ControllerBean> activeControllers = new ArrayList<>();

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            if (controller.isOnosAlive()) {
                activeControllers.add(controller);
            }
        }

        return activeControllers;
    }

    public ArrayList<ControllerBean> getUnderSubControllers(ArrayList<ControllerBean> activeControllers, State state) {
        ArrayList<ControllerBean> underSubControllers = new ArrayList<>();

        long avgNumOFMsgs = getAverageNumOFMsgs(getActiveControllers(), state);

        for (ControllerBean controller : activeControllers) {
            long tmpNumOFMsgs = getNumOFMsgsForSingleController(controller, state);
            if (tmpNumOFMsgs < avgNumOFMsgs) {
                underSubControllers.add(controller);
            }
        }

        return underSubControllers;
    }

    public ArrayList<ControllerBean> getOverSubControllers(ArrayList<ControllerBean> activeControllers, State state) {
        ArrayList<ControllerBean> overSubControllers = new ArrayList<>();

        long avgNumOFMsgs = getAverageNumOFMsgs(getActiveControllers(), state);

        for (ControllerBean controller : activeControllers) {
            long tmpNumOFMsgs = getNumOFMsgsForSingleController(controller, state);
            if (tmpNumOFMsgs >= avgNumOFMsgs) {
                overSubControllers.add(controller);
            }
        }

        return overSubControllers;
    }

    public long getAverageNumOFMsgs(ArrayList<ControllerBean> activeControllers, State state) {
        long sumOFMsgs = 0;

        for (ControllerBean controller : activeControllers) {
            HashMap<String, ControlPlaneTuple> tmpCPTuple = state.getControlPlaneTuples().get(controller.getControllerId());
            for (String dpid : tmpCPTuple.keySet()) {
                for (OFType ofType : OFType.values()) {
                    sumOFMsgs += tmpCPTuple.get(dpid).getControlTrafficResults().get(ofType);
                }
            }
        }

        return sumOFMsgs/activeControllers.size();
    }

    public long getNumOFMsgsForSingleController(ControllerBean targetController, State state) {
        long sumOFMsgs = 0;

        HashMap<String, ControlPlaneTuple> tmpCPTuple = state.getControlPlaneTuples().get(targetController.getControllerId());
        for (String dpid : tmpCPTuple.keySet()) {
            for (OFType ofType : OFType.values()) {
                sumOFMsgs += tmpCPTuple.get(dpid).getControlTrafficResults().get(ofType);
            }
        }

        return sumOFMsgs;
    }

    public long getNumOFMsgsForSingleSwitchInMasterController(ControllerBean masterController, String dpid, State state) {
        long sumOFMsgs = 0;

        for (OFType ofType : OFType.values()) {
            sumOFMsgs += state.getControlPlaneTuples().get(masterController.getControllerId()).get(dpid).getControlTrafficResults().get(ofType);
        }

        return sumOFMsgs;
    }

    public ArrayList<String> getSortedSwitchList (ControllerBean masterController, State state) {
        ArrayList<String> result = new ArrayList<>();

        HashMap<String, ControlPlaneTuple> tmpControlPlaneTuples = state.getControlPlaneTuples().get(masterController.getControllerId());
        HashMap<String, Long> tmpRawOFMsgsForEachSwitch = new HashMap<>();
        HashMap<String, Long> tmpOFMsgsForEachSwitch = new HashMap<>();

        for (String dpid : tmpControlPlaneTuples.keySet()) {
            long tmpNumOFMsgs = getNumOFMsgsForSingleSwitchInMasterController(masterController, dpid, state);
            tmpRawOFMsgsForEachSwitch.putIfAbsent(dpid, tmpNumOFMsgs);
        }

        MastershipTuple mastershipTuple = state.getMastershipTuples().get(masterController.getControllerId());
        for (String dpid : mastershipTuple.getSwitchList()) {
            tmpOFMsgsForEachSwitch.putIfAbsent(dpid, tmpRawOFMsgsForEachSwitch.get(dpid));
        }

        printHashmapForTest(tmpRawOFMsgsForEachSwitch);

        return result;
    }

    public void printHashmapForTest(HashMap<String, Long> hashMap) {
        for (String key : hashMap.keySet()) {
            System.out.println("***" + key + ": " + hashMap.get(key));
        }
    }
}
