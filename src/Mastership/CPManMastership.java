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
        long avgNumOFMsgs = getAverageNumOFMsgs(activeControllers, state);

        // initialize topology
        for (ControllerBean controller : activeControllers) {
            topology.putIfAbsent(controller.getControllerId(), new ArrayList<>());
        }

        // sort over-subscribed switch list
        HashMap<String, ArrayList<String>> sortedSwitchesOverSub = new HashMap<>();
        for (ControllerBean controller : overSubControllers) {
            sortedSwitchesOverSub.putIfAbsent(controller.getControllerId(), getSortedSwitchList(controller, state));
        }

        // Temporal hashmap having # OF msgs for each controller. It is used to estimate # OF msgs when switches are moved to other controller.
        HashMap<ControllerBean, Long> estimatedUnderSubControllerOFMsgs = new HashMap<>();
        HashMap<ControllerBean, Long> estimatedOverSubControllerOFMsgs = new HashMap<>();

        // make former hashmap
        for (ControllerBean underController : underSubControllers) {
            long tmpNumOFMsgs = getNumOFMsgsForSingleController(underController, state);
            estimatedUnderSubControllerOFMsgs.putIfAbsent(underController, tmpNumOFMsgs);
        }
        // make later hashmap
        for (ControllerBean overController : overSubControllers) {
            long tmpNumOFMsgs = getNumOFMsgsForSingleController(overController, state);
            estimatedOverSubControllerOFMsgs.putIfAbsent(overController, tmpNumOFMsgs);
        }

        // Loop: Move a switch
        // - traversal under sub controllers --> traversal over sub controllers for each under sub controllers
        ArrayList<String> tmpOverSubControllers = new ArrayList<>();
        for (ControllerBean controller : overSubControllers) {
            tmpOverSubControllers.add(controller.getControllerId());
        }

        while (tmpOverSubControllers.size() != 0) {
            ControllerBean mostController = getMostOFMsgsController(estimatedOverSubControllerOFMsgs);
            ControllerBean leastController = getLeastOFMsgsController(estimatedUnderSubControllerOFMsgs);
            ArrayList<String> tmpSwitchListInMostController = sortedSwitchesOverSub.get(mostController.getControllerId());
            for (int index = 0; index < tmpSwitchListInMostController.size(); index++) {
                String tmpDpid = tmpSwitchListInMostController.get(index);
                long tmpSwitchOFMsgs = getNumOFMsgsForSingleSwitchInMasterController(mostController, tmpDpid, state);
                long tmpMostControllerOFMsgs = estimatedOverSubControllerOFMsgs.get(mostController);
                long tmpLeastControllerOFMsgs = estimatedUnderSubControllerOFMsgs.get(leastController);
                // what if this switch is moved?
                long tmpMostControllerOFMsgsChanged = tmpMostControllerOFMsgs - tmpSwitchOFMsgs;
                long tmpLeastControllerOFMsgsChanged = tmpLeastControllerOFMsgs + tmpSwitchOFMsgs;

                // can this switch be moved?
                if (tmpMostControllerOFMsgsChanged > avgNumOFMsgs && tmpLeastControllerOFMsgsChanged < avgNumOFMsgs) {
                    tmpSwitchListInMostController.remove(tmpDpid);
                    topology.get(leastController.getControllerId()).add(tmpDpid);
                    estimatedOverSubControllerOFMsgs.replace(mostController, tmpMostControllerOFMsgsChanged);
                    estimatedUnderSubControllerOFMsgs.replace(leastController, tmpLeastControllerOFMsgsChanged);
                    break;
                }
                // if it is last index and there is no movable switch, remove oversubcontroller
                if (index == (tmpSwitchListInMostController.size() - 1)) {
                    tmpOverSubControllers.remove(mostController.getControllerId());
                    estimatedOverSubControllerOFMsgs.remove(mostController);
                }
            }
        }

        // should be uncommented
        changeMultipleMastership(topology);
    }

    public ArrayList<ControllerBean> getActiveControllers() {
        ArrayList<ControllerBean> activeControllers = new ArrayList<>();

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            if (controller.isActive()) {
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

            if (tmpCPTuple == null) {
                sumOFMsgs += 0;
            } else {
                for (String dpid : tmpCPTuple.keySet()) {
                    for (OFType ofType : OFType.values()) {
                        sumOFMsgs += tmpCPTuple.get(dpid).getControlTrafficResults().get(ofType);
                    }
                }
            }
        }

        return sumOFMsgs/activeControllers.size();
    }

    public long getNumOFMsgsForSingleController(ControllerBean targetController, State state) {
        long sumOFMsgs = 0;

        HashMap<String, ControlPlaneTuple> tmpCPTuple = state.getControlPlaneTuples().get(targetController.getControllerId());

        if (tmpCPTuple == null) {
            sumOFMsgs += 0;
        } else {
            for (String dpid : tmpCPTuple.keySet()) {
                for (OFType ofType : OFType.values()) {
                    sumOFMsgs += tmpCPTuple.get(dpid).getControlTrafficResults().get(ofType);
                }
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

        for (String dpid : tmpOFMsgsForEachSwitch.keySet()) {
            if (result.size() == 0) {
                result.add(dpid);
            } else {
                long numOFMsgsForTargetSwitch = tmpOFMsgsForEachSwitch.get(dpid);
                for (int index = 0; index < result.size(); index++) {
                    String tmpDpid = result.get(index);
                    long tmpNumOFMsgsForSwitch = tmpOFMsgsForEachSwitch.get(tmpDpid);
                    if (numOFMsgsForTargetSwitch <= tmpNumOFMsgsForSwitch) {
                        if (index == (result.size() -1)) {
                            result.add(dpid);
                            break;
                        }
                        continue;
                    } else {
                        result.add(index, dpid);
                        break;
                    }
                }
            }
        }

        return result;
    }

    public ControllerBean getMostOFMsgsController(HashMap<ControllerBean, Long> estimatedControllerOFMsgs) {
        ControllerBean tmpController = null;

        for (ControllerBean controller : estimatedControllerOFMsgs.keySet()) {
            if (tmpController == null) {
                tmpController = controller;
            } else {
                if (estimatedControllerOFMsgs.get(controller) > estimatedControllerOFMsgs.get(tmpController)) {
                    tmpController = controller;
                }
            }
        }

        return tmpController;
    }

    public ControllerBean getLeastOFMsgsController (HashMap<ControllerBean, Long> estimatedControllerOFMsgs) {
        ControllerBean tmpController = null;

        for (ControllerBean controller : estimatedControllerOFMsgs.keySet()) {
            if (tmpController == null) {
                tmpController = controller;
            } else {
                if (estimatedControllerOFMsgs.get(controller) < estimatedControllerOFMsgs.get(tmpController)) {
                    tmpController = controller;
                }
            }
        }

        return tmpController;
    }

    public void printHashmapForTest(HashMap<String, Long> hashMap) {
        for (String key : hashMap.keySet()) {
            System.out.println("***" + key + ": " + hashMap.get(key));
        }
    }

    public void printArrayListForTest(ArrayList<String> arrayList) {
        for (String value : arrayList) {
            System.out.println(value);
        }
    }
}
