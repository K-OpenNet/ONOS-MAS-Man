package Database.Tuples;

import java.util.ArrayList;

public class MastershipTuple extends AbstractTuple implements Tuple {

    private ArrayList<String> switchList;

    public MastershipTuple() {
        tupleName = tupleType.MASTERSHIPTUPLE;
        switchList = new ArrayList<>();
    }

    public boolean hasSwitch(String switchKey) {
        return switchList.contains(switchKey);
    }

    public void addSwitch(String switchKey) {
        switchList.add(switchKey);
    }

    public void delSwitch(String switchKey) {
        switchList.remove(switchKey);
    }

    public ArrayList<String> getSwitchList() {
        return switchList;
    }

    public void setSwitchList(ArrayList<String> switchList) {
        this.switchList = switchList;
    }
}
