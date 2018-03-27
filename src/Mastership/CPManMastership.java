package Mastership;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tables.State;

import java.util.ArrayList;
import java.util.HashMap;

public class CPManMastership extends AbstractMastership implements Mastership {
    public CPManMastership() {
        mastershipName = mastershipType.CPMAN;
    }

    @Override
    public void runMastershipAlgorithm(State state) {
        HashMap<String, ArrayList<String>> topology = new HashMap<>();

        ArrayList<String> activeControllers = getActiveControllers();
        ArrayList<String> underSubControllers = getUnderSubControllers();
        ArrayList<String> overSubControllers = getOverSubControllers();

        changeMultipleMastership(topology);
    }

    public ArrayList<String> getActiveControllers() {
        ArrayList<String> activeControllers = new ArrayList<>();

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            if (controller.isOnosAlive()) {
                activeControllers.add(controller.getControllerId());
            }
        }

        return activeControllers;
    }

    public ArrayList<String> getUnderSubControllers() {
        ArrayList<String> underSubControllers = new ArrayList<>();

        return underSubControllers;
    }

    public ArrayList<String> getOverSubControllers() {
        ArrayList<String> overSubControllers = new ArrayList<>();

        return overSubControllers;
    }

}
