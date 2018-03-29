package Database.Tables;

import Beans.ControllerBean;
import Database.Configure.Configuration;

import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    private static Database ourInstance = new Database();
    private static ArrayList<State> database = new ArrayList<>();

    public static Database getInstance() {
        return ourInstance;
    }

    private Database() {
    }

    public State getAllTuples(int timeIndex) {
        return database.get(timeIndex);
    }

    // DB print functions
    // Overall
    public String getOverallTuples() {

        String results = "";
        HashMap<String, String> resultMastership = new HashMap<>();
        HashMap<String, String> resultCPULoad = new HashMap<>();
        HashMap<String, String> resultMemLoad = new HashMap<>();
        HashMap<String, String> resultNetRx = new HashMap<>();
        HashMap<String, String> resultNetTx = new HashMap<>();
        HashMap<String, String> resultNetBandwidth = new HashMap<>();
        HashMap<String, String> resultNumOfMsgs = new HashMap<>();
        HashMap<String, String> resultByteOfMsgs = new HashMap<>();
        HashMap<String, String> resultNumCPUs = new HashMap<>();
        HashMap<String, String> resultActiveFlages = new HashMap<>();

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            resultMastership.putIfAbsent(controller.getBeanKey(), ":\t");
            resultCPULoad.putIfAbsent(controller.getBeanKey(), ":\t");
            resultMemLoad.putIfAbsent(controller.getBeanKey(), ":\t");
            resultNetRx.putIfAbsent(controller.getBeanKey(), ":\t");
            resultNetTx.putIfAbsent(controller.getBeanKey(), ":\t");
            resultNetBandwidth.putIfAbsent(controller.getBeanKey(), ":\t");
            resultNumOfMsgs.putIfAbsent(controller.getBeanKey(), ":\t");
            resultByteOfMsgs.putIfAbsent(controller.getBeanKey(), ":\t");
            resultNumCPUs.putIfAbsent(controller.getBeanKey(), ":\t");
            resultActiveFlages.putIfAbsent(controller.getBeanKey(), ":\t");
        }

        ArrayList<State> tmpDatabase = (ArrayList<State>) database.clone();

        for (State state : tmpDatabase) {
            for (ControllerBean controller : Configuration.getInstance().getControllers()) {
                // Mastership

                String tmp;
                try {
                    tmp = resultMastership.get(controller.getBeanKey()) + "\t"
                            + state.getMastershipTuples().get(controller.getBeanKey()).getSwitchList().size();
                } catch (NullPointerException e) {
                    tmp = resultMastership.get(controller.getBeanKey()) + "\t" + 0;
                }

                resultMastership.replace(controller.getBeanKey(), tmp);

                // Computing Resource
                // CPU load
                try {
                    tmp = resultCPULoad.get(controller.getBeanKey()) + "\t"
                            + state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage();
                } catch (NullPointerException e) {
                    tmp = resultCPULoad.get(controller.getBeanKey()) + "\t" + 0;
                }
                resultCPULoad.replace(controller.getBeanKey(), tmp);

                // MEM load
                try {
                    tmp = resultMemLoad.get(controller.getBeanKey()) + "\t"
                            + state.getComputingResourceTuples().get(controller.getBeanKey()).avgRamUsage();

                } catch (NullPointerException e) {
                    tmp = resultMemLoad.get(controller.getBeanKey()) + "\t" + 0;
                }
                resultMemLoad.replace(controller.getBeanKey(), tmp);

                // Net Rx load
                try {
                    tmp = resultNetRx.get(controller.getBeanKey()) + "\t"
                            + state.getComputingResourceTuples().get(controller.getBeanKey()).avgNetRx();

                } catch (NullPointerException e) {
                    tmp = resultNetRx.get(controller.getBeanKey()) + "\t" + 0;
                }
                resultNetRx.replace(controller.getBeanKey(), tmp);

                // Net Tx load
                try {
                    tmp = resultNetTx.get(controller.getBeanKey()) + "\t"
                            + state.getComputingResourceTuples().get(controller.getBeanKey()).avgNetTx();

                } catch (NullPointerException e) {
                    tmp = resultNetTx.get(controller.getBeanKey()) + "\t" + 0;
                }
                resultNetTx.replace(controller.getBeanKey(), tmp);

                // Net Bandwidth load
                try {
                    tmp = resultNetBandwidth.get(controller.getBeanKey()) + "\t"
                            + state.getComputingResourceTuples().get(controller.getBeanKey()).avgNetBandwidth();

                } catch (NullPointerException e) {
                    tmp = resultNetBandwidth.get(controller.getBeanKey()) + "\t" + 0;
                }
                resultNetBandwidth.replace(controller.getBeanKey(), tmp);

                // Control Plane
                // Num OF msgs
                try {
                    long tmpNumMsg = 0;
                    for (String dpid : state.getControlPlaneTuples().get(controller.getBeanKey()).keySet()) {
                        tmpNumMsg += state.getControlPlaneTuples().get(controller.getBeanKey()).get(dpid).totalControlTrafficMessages();
                    }
                    tmp = resultNumOfMsgs.get(controller.getBeanKey()) + "\t" + tmpNumMsg;
                } catch (NullPointerException e) {
                    tmp = resultNumOfMsgs.get(controller.getBeanKey()) + "\t" + 0;
                }

                resultNumOfMsgs.replace(controller.getBeanKey(), tmp);

                // Bytes OF msgs
                try {
                    long tmpByteMsg = 0;
                    for (String dpid : state.getControlPlaneTuples().get(controller.getBeanKey()).keySet()) {
                        tmpByteMsg += state.getControlPlaneTuples().get(controller.getBeanKey()).get(dpid).totalControlTrafficBytes();
                    }
                    tmp = resultByteOfMsgs.get(controller.getBeanKey()) + "\t" + tmpByteMsg;
                } catch (NullPointerException e) {
                    tmp = resultByteOfMsgs.get(controller.getBeanKey()) + "\t" + 0;
                }

                resultByteOfMsgs.replace(controller.getBeanKey(), tmp);

                // # CPUs
                tmp = resultNumCPUs.get(controller.getBeanKey()) + "\t"
                        + state.getNumCPUsTuples().get(controller.getBeanKey());
                resultNumCPUs.replace(controller.getBeanKey(), tmp);

                // active flags
                tmp = resultActiveFlages.get(controller.getBeanKey()) + "\t"
                        + state.getActiveFlags().get(controller.getBeanKey());
                resultActiveFlages.replace(controller.getBeanKey(), tmp);
            }
        }
        results += "Index\t";
        for (int index = 0; index < database.size(); index++) {
            results = results + index + "\t";
        }
        results += "\n";

        results += "Mastership results\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultMastership.get(controller.getBeanKey());
            results += "\n";
        }

        results += "CPU load results\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultCPULoad.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Mem load results\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultMemLoad.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Net Rx load results\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultNetRx.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Net Tx load results\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultNetTx.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Net bandwidth load results\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultNetBandwidth.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Control plane - Num OF msgs\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultNumOfMsgs.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Control plane - Bytes OF msgs\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultByteOfMsgs.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Num CPUs\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultNumCPUs.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Active/Inactive Flags\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultActiveFlages.get(controller.getBeanKey());
            results += "\n";
        }

        return results;
    }

    public String getSingleTuple(State state) {

        String results = "";
        HashMap<String, String> resultMastership = new HashMap<>();
        HashMap<String, String> resultCPULoad = new HashMap<>();
        HashMap<String, String> resultMemLoad = new HashMap<>();
        HashMap<String, String> resultNetRx = new HashMap<>();
        HashMap<String, String> resultNetTx = new HashMap<>();
        HashMap<String, String> resultNetBandwidth = new HashMap<>();
        HashMap<String, String> resultNumOfMsgs = new HashMap<>();
        HashMap<String, String> resultByteOfMsgs = new HashMap<>();
        HashMap<String, String> resultNumCPUs = new HashMap<>();
        HashMap<String, String> resultActiveFlages = new HashMap<>();

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            resultMastership.putIfAbsent(controller.getBeanKey(), ":\t");
            resultCPULoad.putIfAbsent(controller.getBeanKey(), ":\t");
            resultMemLoad.putIfAbsent(controller.getBeanKey(), ":\t");
            resultNetRx.putIfAbsent(controller.getBeanKey(), ":\t");
            resultNetTx.putIfAbsent(controller.getBeanKey(), ":\t");
            resultNetBandwidth.putIfAbsent(controller.getBeanKey(), ":\t");
            resultNumOfMsgs.putIfAbsent(controller.getBeanKey(), ":\t");
            resultByteOfMsgs.putIfAbsent(controller.getBeanKey(), ":\t");
            resultNumCPUs.putIfAbsent(controller.getBeanKey(), ":\t");
            resultActiveFlages.putIfAbsent(controller.getBeanKey(), ":\t");
        }


        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            // Mastership
            String tmp = resultMastership.get(controller.getBeanKey()) + "\t"
                    + state.getMastershipTuples().get(controller.getBeanKey()).getSwitchList().size();
            resultMastership.replace(controller.getBeanKey(), tmp);

            // Computing Resource
            // CPU load
            tmp = resultCPULoad.get(controller.getBeanKey()) + "\t"
                    + state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage();
            resultCPULoad.replace(controller.getBeanKey(), tmp);

            // MEM load
            tmp = resultMemLoad.get(controller.getBeanKey()) + "\t"
                    + state.getComputingResourceTuples().get(controller.getBeanKey()).avgRamUsage();
            resultMemLoad.replace(controller.getBeanKey(), tmp);

            // Net Rx load
            tmp = resultNetRx.get(controller.getBeanKey()) + "\t"
                    + state.getComputingResourceTuples().get(controller.getBeanKey()).avgNetRx();
            resultNetRx.replace(controller.getBeanKey(), tmp);

            // Net Tx load
            tmp = resultNetTx.get(controller.getBeanKey()) + "\t"
                    + state.getComputingResourceTuples().get(controller.getBeanKey()).avgNetTx();
            resultNetTx.replace(controller.getBeanKey(), tmp);

            // Net Bandwidth load
            tmp = resultNetBandwidth.get(controller.getBeanKey()) + "\t"
                    + state.getComputingResourceTuples().get(controller.getBeanKey()).avgNetBandwidth();
            resultNetBandwidth.replace(controller.getBeanKey(), tmp);

            // Control Plane
            // Num OF msgs
            long tmpNumMsg = 0;
            for (String dpid : state.getControlPlaneTuples().get(controller.getBeanKey()).keySet()) {
                tmpNumMsg += state.getControlPlaneTuples().get(controller.getBeanKey()).get(dpid).totalControlTrafficMessages();
            }
            tmp = resultNumOfMsgs.get(controller.getBeanKey()) + "\t" + tmpNumMsg;
            resultNumOfMsgs.replace(controller.getBeanKey(), tmp);

            // Bytes OF msgs
            long tmpByteMsg = 0;
            for (String dpid : state.getControlPlaneTuples().get(controller.getBeanKey()).keySet()) {
                tmpByteMsg += state.getControlPlaneTuples().get(controller.getBeanKey()).get(dpid).totalControlTrafficBytes();
            }
            tmp = resultByteOfMsgs.get(controller.getBeanKey()) + "\t" + tmpByteMsg;
            resultByteOfMsgs.replace(controller.getBeanKey(), tmp);

            // # CPUs
            tmp = resultNumCPUs.get(controller.getBeanKey()) + "\t"
                    + state.getNumCPUsTuples().get(controller.getBeanKey());
            resultNumCPUs.replace(controller.getBeanKey(), tmp);

            // active flags
            tmp = resultActiveFlages.get(controller.getBeanKey()) + "\t"
                    + state.getActiveFlags().get(controller.getBeanKey());
            resultActiveFlages.replace(controller.getBeanKey(), tmp);
        }

        results += "Index\t";
        for (int index = 0; index < database.size(); index++) {
            results = results + index + "\t";
        }
        results += "\n";

        results += "Mastership results\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultMastership.get(controller.getBeanKey());
            results += "\n";
        }

        results += "CPU load results\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultCPULoad.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Mem load results\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultMemLoad.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Net Rx load results\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultNetRx.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Net Tx load results\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultNetTx.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Net bandwidth load results\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultNetBandwidth.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Control plane - Num OF msgs\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultNumOfMsgs.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Control plane - Bytes OF msgs\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultByteOfMsgs.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Num CPUs\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results = results + controller.getBeanKey() + resultNumCPUs.get(controller.getBeanKey());
            results += "\n";
        }

        results += "Active/Inactive Flags\n";
        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            results += results + controller.getBeanKey() + resultActiveFlages.get(controller.getBeanKey());
            results += "\n";
        }

        return results;
    }

    public static Database getOurInstance() {
        return ourInstance;
    }

    public static void setOurInstance(Database ourInstance) {
        Database.ourInstance = ourInstance;
    }

    public static ArrayList<State> getDatabase() {
        return database;
    }

    public static void setDatabase(ArrayList<State> database) {
        Database.database = database;
    }
}
