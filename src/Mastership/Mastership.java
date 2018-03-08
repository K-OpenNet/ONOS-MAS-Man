package Mastership;

import Beans.ControllerBean;

public interface Mastership {
    enum mastershipType {
        EQUALMASTERSHIP, CPMAN;
    }

    void runMastershipAlgorithm();
    void changeMastership(String dpid, ControllerBean targetController);
}
