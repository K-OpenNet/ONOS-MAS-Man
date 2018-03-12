package Mastership;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tables.State;
import Utils.Connection.RESTConnection;

import java.util.ArrayList;

public class EqualizingMastership extends AbstractMastership implements Mastership {

    public EqualizingMastership() {
        mastershipName = mastershipType.EQUALMASTERSHIP;
    }

    @Override
    public void runMastershipAlgorithm(ArrayList<State> dbDump) {

        ControllerBean target = null;

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            if (controller.isOnosAlive()) {
                target = controller;
                break;
            }
        }

        if (target == null) {
            throw new NullPointerException();
        }

        RESTConnection conn = new RESTConnection();
        conn.sendCommandToUser(target, Configuration.RESTURL_DOEQUALIZE);

    }
}
