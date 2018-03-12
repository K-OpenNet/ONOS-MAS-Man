package Mastership;

import Beans.ControllerBean;
import Database.Tables.State;

import java.util.ArrayList;

public interface Mastership {
    enum mastershipType {
        EQUALMASTERSHIP, CPMAN;
    }

    void runMastershipAlgorithm(State state);
    void changeMastership(String dpid, ControllerBean targetController);
}
