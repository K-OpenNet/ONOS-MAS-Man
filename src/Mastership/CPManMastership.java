package Mastership;

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
