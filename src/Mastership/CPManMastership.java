package Mastership;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tables.State;
import Database.Tuples.ControlPlaneTuple;
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
        ArrayList<ControllerBean> underSubControllers = getUnderSubControllers(state);
        ArrayList<ControllerBean> overSubControllers = getOverSubControllers(state);

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

    public ArrayList<ControllerBean> getUnderSubControllers(State state) {
        ArrayList<ControllerBean> underSubControllers = new ArrayList<>();

        long avgNumOFMsgs = getAverageNumOFMsgs(getActiveControllers(), state);

        return underSubControllers;
    }

    public ArrayList<ControllerBean> getOverSubControllers(State state) {
        ArrayList<ControllerBean> overSubControllers = new ArrayList<>();

        long avgNumOFMsgs = getAverageNumOFMsgs(getActiveControllers(), state);

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

}
