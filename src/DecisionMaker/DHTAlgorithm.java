package DecisionMaker;

import Beans.ControllerBean;
import Database.Configure.Configuration;
import Database.Tables.State;
import Mastership.CPManMastership;

import java.util.ArrayList;

import static Database.Configure.Configuration.MAX_NET_BANDWIDTH;
import static Database.Configure.Configuration.SCALING_THRESHOLD_LOWER;
import static Database.Configure.Configuration.SCALING_THRESHOLD_UPPER;

public class DHTAlgorithm extends AbstractDecisionMaker implements DecisionMaker {

    public DHTAlgorithm() {
        decisionMakerName = decisionMakerType.SCALING_DHT;
    }

    @Override
    public void runDecisionMakerAlgorithm(int currentTimeIndex, ArrayList<State> dbDump) {
        if (currentTimeIndex == 0) {
            // Configuration algorithm
            initConfigurationAlgorithm(dbDump.get(0));
            return;
        }

        ArrayList<State> targetStates = new ArrayList<>();

        int startPoint = currentTimeIndex - Configuration.NOSCALING_CPMAN_PERIOD + 1;
        int endPoint = currentTimeIndex;

        for (int index = startPoint; index <= endPoint; index++) {
            targetStates.add(dbDump.get(index));
        }

        State state = mergeStates(targetStates);

        CPManMastership mastership = new CPManMastership();
        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();

        double maxNetLoad = getMaxNetworkLoad(state, activeControllers) * 8;
        double avgNetLoad = getAvgNetworkLoad(state, activeControllers) * 8;
        maxNetLoad = maxNetLoad / 1000000;
        avgNetLoad = avgNetLoad / 1000000;

        double upperThreshold = (double) MAX_NET_BANDWIDTH * ((double) SCALING_THRESHOLD_UPPER/100);
        double lowerThreshold = (double) MAX_NET_BANDWIDTH * ((double) SCALING_THRESHOLD_LOWER/100);

        System.out.println("MAX Load: " + maxNetLoad + " / Avg Load: " + avgNetLoad);
        System.out.println("Up Threshold: " + upperThreshold + " / Bottom Threshold: " + lowerThreshold);


    }

    public void initConfigurationAlgorithm(State state) {

        CPManMastership mastership = new CPManMastership();
        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();

        double maxNetLoad = getMaxNetworkLoad(state, activeControllers) * 8;
        double avgNetLoad = getAvgNetworkLoad(state, activeControllers) * 8;
        maxNetLoad = maxNetLoad / 1000000;
        avgNetLoad = avgNetLoad / 1000000;

        double upperThreshold = (double) MAX_NET_BANDWIDTH * ((double) SCALING_THRESHOLD_UPPER/100);
        double lowerThreshold = (double) MAX_NET_BANDWIDTH * ((double) SCALING_THRESHOLD_LOWER/100);

        System.out.println("MAX Load: " + maxNetLoad + " / Avg Load: " + avgNetLoad);
        System.out.println("Up Threshold: " + upperThreshold + " / Bottom Threshold: " + lowerThreshold);

    }

    public double getTotalNetworkLoad(State state, ArrayList<ControllerBean> activeControllers) {

        double result = 0.0;

        for (ControllerBean controller : activeControllers) {
            result += state.getComputingResourceTuples().get(controller.getBeanKey()).avgNet();
        }

        return result;
    }

    public double getAvgNetworkLoad(State state, ArrayList<ControllerBean> activeControllers) {
        return getTotalNetworkLoad(state, activeControllers)/activeControllers.size();
    }

    public double getMaxNetworkLoad(State state, ArrayList<ControllerBean> activeControllers) {

        double result = 0.0;

        for (ControllerBean controller : activeControllers) {

            double tmpNetLoad = state.getComputingResourceTuples().get(controller.getBeanKey()).avgNet();
            if (tmpNetLoad > result) {

                result = tmpNetLoad;
            }
        }

        return result;
    }
}
