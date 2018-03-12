package Mastership;

import Database.Tables.State;

import java.util.ArrayList;

public class CPManMastership extends AbstractMastership implements Mastership {
    public CPManMastership() {
        mastershipName = mastershipType.CPMAN;
    }

    @Override
    public void runMastershipAlgorithm(ArrayList<State> dbDump) {

    }
}
