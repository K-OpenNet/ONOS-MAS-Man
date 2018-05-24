package Scaling;

import Beans.ControllerBean;
import Beans.MininetBean;
import Beans.PMBean;
import Database.Configure.Configuration;
import Database.Tables.State;
import Database.Tuples.MastershipTuple;
import DecisionMaker.DecisionMaker;
import Mastership.CPManMastership;
import Utils.Connection.RESTConnection;
import Utils.Connection.SSHConnection;

import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.HashMap;

import static Database.Configure.Configuration.*;

public class ControllerScaling extends AbstractScaling implements Scaling {

    public ControllerScaling() {
        scalingName = scalingType.CONTROLLERSCALING;
    }

    public void runL1ONOSScaleIn(ControllerBean targetController, State state) {
        if (targetController.getControllerId().equals(Configuration.FIXED_CONTROLLER_ID_1) ||
                targetController.getControllerId().equals(Configuration.FIXED_CONTROLLER_ID_2) ||
                targetController.getControllerId().equals(Configuration.FIXED_CONTROLLER_ID_3)) {
            throw new TurnOffFixedControllerException();
        }

        if (DECISIONMAKER_TYPE == DecisionMaker.decisionMakerType.SCALING_CPU) {
            distributeMastershipForScaleInElastiCon(targetController, state);
        } else {
            distributeMastershipForScaleInAES(targetController, state);
        }
        targetController.setActive(false);
    }

    public void runL2ONOSScaleIn(ControllerBean targetController, State state) {
        if (targetController.getControllerId().equals(Configuration.FIXED_CONTROLLER_ID_1) ||
                targetController.getControllerId().equals(Configuration.FIXED_CONTROLLER_ID_2) ||
                targetController.getControllerId().equals(Configuration.FIXED_CONTROLLER_ID_3)) {
            throw new TurnOffFixedControllerException();
        }

        targetController.setOnosAlive(false);
        runL1ONOSScaleIn(targetController, state);
        switchOffControllerForScaleIn(targetController, state);
    }

    public void runL3ONOSScaleIn(ControllerBean targetController, State state) {
        if (targetController.getControllerId().equals(Configuration.FIXED_CONTROLLER_ID_1) ||
                targetController.getControllerId().equals(Configuration.FIXED_CONTROLLER_ID_2) ||
                targetController.getControllerId().equals(Configuration.FIXED_CONTROLLER_ID_3)) {
            throw new TurnOffFixedControllerException();
        }

        targetController.setVmAlive(false);
        runL2ONOSScaleIn(targetController, state);
        switchOffVMForScaleIn(targetController, state);
    }

    public void runL1ONOSScaleOut(ControllerBean targetController, State state) {
        targetController.setActive(true);
        if (DECISIONMAKER_TYPE == DecisionMaker.decisionMakerType.SCALING_CPU) {
            distributeMastershipForScaleOutElastiCon(targetController, state);
        } else {
            distributeMastershipForScaleOutAES(targetController, state);
        }
    }

    public void runL2ONOSScaleOut(ControllerBean targetController, State state) {
        switchOnControllerForScaleOut(targetController, state);
        targetController.setOnosAlive(true);
        runL1ONOSScaleOut(targetController, state);
    }

    public void runL3ONOSScaleOut(ControllerBean targetController, State state) {
        switchOnVMForScaleOut(targetController, state);
        targetController.setVmAlive(true);
        runL2ONOSScaleOut(targetController, state);
    }

    public void distributeMastershipForScaleInAES(ControllerBean targetController, State state) {

        CPManMastership mastership = new CPManMastership();

        ArrayList<ControllerBean> otherControllers = mastership.getActiveControllers();
        otherControllers.remove(targetController);

        HashMap<String, ArrayList<String>> topology = new HashMap<>();
        for (ControllerBean controller : otherControllers) {
            topology.putIfAbsent(controller.getControllerId(), new ArrayList<>());
        }

        // get switches
        ArrayList<String> switchesInTargetController = mastership.getSortedSwitchList(targetController, state);

        // make estimated hashmap variable for other controllers
        HashMap<ControllerBean, Long> estimatedOtherControllerOFMsgs = new HashMap<>();
        for (ControllerBean controller : otherControllers) {
            long tmpNumOFMsgs = mastership.getNumOFMsgsForSingleController(controller, state);
            estimatedOtherControllerOFMsgs.putIfAbsent(controller, tmpNumOFMsgs);
        }

        // distribute from target to other active controllers
        for (String dpid : switchesInTargetController) {
            long tmpOFMsgs = mastership.getNumOFMsgsForSingleSwitchInMasterController(targetController, dpid, state);
            ControllerBean leastController = mastership.getLeastOFMsgsController(estimatedOtherControllerOFMsgs);
            long prevOFMsgs = estimatedOtherControllerOFMsgs.get(leastController);
            long changedOFMsgs = prevOFMsgs + tmpOFMsgs;
            estimatedOtherControllerOFMsgs.replace(leastController, changedOFMsgs);
            topology.get(leastController.getControllerId()).add(dpid);
        }

        mastership.changeMultipleMastership(topology);
    }


//    public void distributeMastershipForScaleInElastiCon(ControllerBean targetController, State state) {
//
//        CPManMastership mastership = new CPManMastership();
//        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();
//        activeControllers.remove(targetController);
//        int numSwitchesInTarget = state.getMastershipTuples().get(targetController.getBeanKey()).getSwitchList().size();
//        double cpuLoadForEachSwitch = state.getComputingResourceTuples().get(targetController.getBeanKey()).avgCpuUsage()/numSwitchesInTarget;
//        ArrayList<String> masterSwitchListInTargetController = (ArrayList<String>) state.getMastershipTuples().get(targetController.getBeanKey()).getSwitchList().clone();
//
//        HashMap<ControllerBean, Double> estimatedControllersCPULoad = new HashMap<>();
//        for (ControllerBean controller : activeControllers) {
//            estimatedControllersCPULoad.put(controller, state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage());
//        }
//
//        HashMap<String, ArrayList<String>> topology = new HashMap<>();
//        for (ControllerBean controller : activeControllers) {
//            topology.putIfAbsent(controller.getControllerId(), new ArrayList<>());
//        }
//
//        for (int index = 0; index < numSwitchesInTarget; index++) {
//            ControllerBean lowestCPULoadController = getLowestCPULoadController(estimatedControllersCPULoad);
//            double tmpCPULoad = estimatedControllersCPULoad.get(lowestCPULoadController) + cpuLoadForEachSwitch;
//            estimatedControllersCPULoad.replace(lowestCPULoadController, tmpCPULoad);
//            System.out.println(masterSwitchListInTargetController.size());
//            topology.get(lowestCPULoadController.getControllerId()).add(masterSwitchListInTargetController.get(index));
//        }
//
//        mastership.changeMultipleMastership(topology);
//    }


    public double sumCPULoadAllControllers(State state, ArrayList<ControllerBean> activeControllers) {
        double result = 0.0;

//        CPManMastership mastership = new CPManMastership();
//        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();
//
//        for (ControllerBean controller : activeControllers) {
//            double tmpCPULoad = state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage();
//            double cpuNormalizeFactor = 40/controller.getNumCPUs();
//            tmpCPULoad = tmpCPULoad * cpuNormalizeFactor;
//            result += tmpCPULoad;
//        }

        CPManMastership mastership = new CPManMastership();

        for (ControllerBean controller : activeControllers) {
            double tmpCPULoad = state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage();
            double cpuNormalizeFactor = 40/controller.getNumCPUs();
            tmpCPULoad = tmpCPULoad * cpuNormalizeFactor;

            if (tmpCPULoad > 100.0) {
                tmpCPULoad = 100;
            }

            result += tmpCPULoad;
        }


        return result;
    }

    public double averageCPUloadWithoutTargetController(State state, ControllerBean targetController) {
        CPManMastership mastership = new CPManMastership();
        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();
        ControllerBean tmpTargetController = null;
        for (ControllerBean controller : activeControllers) {
            if (controller.getControllerId().equals(targetController)) {
                tmpTargetController = controller;
            }
        }

        if (tmpTargetController != null) {
            activeControllers.remove(tmpTargetController);
        }

        double result = sumCPULoadAllControllers(state, activeControllers);

        return result/activeControllers.size();
    }

    public double averageCPUloadWithTargetController(State state, ControllerBean targetController) {
        CPManMastership mastership = new CPManMastership();
        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();
        activeControllers.add(targetController);

        double result = sumCPULoadAllControllers(state, activeControllers);

        return result/activeControllers.size();
    }

    public ControllerBean getLowestCPULoadController (State state, ArrayList<ControllerBean> activeControllers, HashMap<ControllerBean, Double> additionalCPULoads) {

        double minCPULoads = 0.0;
        ControllerBean resultController = null;

        for (ControllerBean controller : activeControllers) {
            double tmpCPUNormalizeFactor = 40/controller.getNumCPUs();
            double tmpCPULoad = state.getComputingResourceTuples().get(controller.getControllerId()).avgCpuUsage();
            tmpCPULoad = tmpCPULoad * tmpCPUNormalizeFactor;
            tmpCPULoad = tmpCPULoad + additionalCPULoads.get(controller);
            if (tmpCPULoad > 100.0) {
                tmpCPULoad = 100;
            }
            if (resultController == null || minCPULoads > tmpCPULoad) {
                resultController = controller;
                minCPULoads = tmpCPULoad;
            }
        }

        return resultController;
    }

    public ControllerBean getHighestCPULoadController (State state, ArrayList<ControllerBean> activeControllers, HashMap<ControllerBean, Double> removedCPULoads) {

        double maxCPULoads = 0.0;
        ControllerBean resultController = null;

        for (ControllerBean controller : activeControllers) {
            double tmpCPUNormalizeFactor = 40/controller.getNumCPUs();
            double tmpCPULoad = state.getComputingResourceTuples().get(controller.getControllerId()).avgCpuUsage();
            tmpCPULoad = tmpCPULoad * tmpCPUNormalizeFactor;
            if (tmpCPULoad > 100.0) {
                tmpCPULoad = 100;
            }
            tmpCPULoad = tmpCPULoad - removedCPULoads.get(controller);
            if (resultController == null || maxCPULoads < tmpCPULoad) {
                resultController = controller;
                maxCPULoads = tmpCPULoad;
            }
        }

        return resultController;
    }

    public void distributeMastershipForScaleInElastiCon(ControllerBean targetController, State state) {

        HashMap<String, ArrayList<String>> topology = new HashMap<>();
        CPManMastership mastership = new CPManMastership();

        HashMap<ControllerBean, Double> additionalCPULoads = new HashMap<>();
        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();
        activeControllers.remove(targetController);

        // init topology
        for (ControllerBean controller : activeControllers) {
            topology.put(controller.getControllerId(), new ArrayList<>());
            additionalCPULoads.put(controller, 0.0);
        }

        double averageCPULoads = averageCPUloadWithoutTargetController(state, targetController);
        double cpuLoadsTargetController = state.getComputingResourceTuples().get(targetController.getBeanKey()).avgCpuUsage();
        double cpuNoarmalizeFactor = 40/targetController.getNumCPUs();
        cpuLoadsTargetController = cpuLoadsTargetController * cpuNoarmalizeFactor;
        ArrayList<String> dpids = state.getMastershipTuples().get(targetController.getBeanKey()).getSwitchList();
        int numSwitches = dpids.size();
        double cpuLoadEachSwitch = cpuLoadsTargetController/numSwitches;

        for (ControllerBean controller : activeControllers) {
            double tmpCPULoads = state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage();
            double tmpCPUNormalizeFactor = 40/controller.getNumCPUs();
            tmpCPULoads = tmpCPUNormalizeFactor * tmpCPULoads;

            if (tmpCPULoads > 100.0) {
                tmpCPULoads = 100;
            }

            if (dpids.size() == 0) {
                break;
            } else if (tmpCPULoads + cpuLoadEachSwitch > averageCPULoads ) {
                break;
            }

            int maxNumSwitches = dpids.size();
            ArrayList<String> changedSwitches = new ArrayList<>();
            for (int index = 0; index < maxNumSwitches; index++) {
                String dpid = dpids.get(index);

                if (tmpCPULoads + cpuLoadEachSwitch <= averageCPULoads &&
                        dpids.size() > changedSwitches.size()) {
                    tmpCPULoads += cpuLoadEachSwitch;
                    changedSwitches.add(dpid);
                    topology.get(controller.getControllerId()).add(dpid);
                }
            }

            additionalCPULoads.put(controller, cpuLoadEachSwitch*changedSwitches.size());

            for (String dpid : changedSwitches) {
                dpids.remove(dpid);
            }
        }

        while (dpids.size() != 0) {
            String dpid = dpids.get(0);
            dpids.remove(dpid);
            ControllerBean lowestController = getLowestCPULoadController(state, activeControllers, additionalCPULoads);
            topology.get(lowestController.getControllerId()).add(dpid);
            additionalCPULoads.replace(lowestController, additionalCPULoads.get(lowestController) + cpuLoadEachSwitch);
        }

        // debugging code
        System.out.println("CPU/switches: " + cpuLoadEachSwitch);
        System.out.println("Topology");
        for (String controllerId : topology.keySet()) {
            System.out.print(controllerId + ": ");
            for (String dpid : topology.get(controllerId)) {
                System.out.print(dpid + " ");
            }
            System.out.println();
        }

        mastership.changeMultipleMastership(topology);
    }

    public ControllerBean getLowestCPULoadController(HashMap<ControllerBean, Double> estResult) {
        double lowestCPULoad = 0.0;
        ControllerBean resultController = null;

        for (ControllerBean controller : estResult.keySet()) {
            if (resultController == null) {
                resultController = controller;
            } else if (lowestCPULoad > estResult.get(controller)) {
                lowestCPULoad = estResult.get(controller);
                resultController = controller;
            }
        }

        return resultController;
    }

    public void distributeMastershipForScaleOutAES(ControllerBean targetController, State state) {
        if (!targetController.isOnosAlive()) {
            throw new L1TargetControllerSanityException();
        }

        CPManMastership mastership = new CPManMastership();

        ArrayList<ControllerBean> otherControllers = mastership.getActiveControllers();
        otherControllers.remove(targetController);

        HashMap<String, ArrayList<String>> topology = new HashMap<>();
        topology.put(targetController.getControllerId(), new ArrayList<>());

        HashMap<String, ArrayList<String>> sortedSwitches = new HashMap<>();
        for (ControllerBean controller : otherControllers) {
            sortedSwitches.putIfAbsent(controller.getControllerId(), mastership.getSortedSwitchList(controller, state));
        }

        long totalOFMsgs = 0;

        HashMap<ControllerBean, Long> estimatedOtherControllerOFMsgs = new HashMap<>();
        for (ControllerBean controller : otherControllers) {
            long tmpNumOFMsgs = mastership.getNumOFMsgsForSingleController(controller, state);
            totalOFMsgs += tmpNumOFMsgs;
            estimatedOtherControllerOFMsgs.putIfAbsent(controller, tmpNumOFMsgs);
        }

        int numTotalControllers = 1 + otherControllers.size();
        long avgOFMsgs = totalOFMsgs / numTotalControllers;
        long targetControllerOFMsgs = 0;

        while (otherControllers.size() != 0) {
            ControllerBean mostController = mastership.getMostOFMsgsController(estimatedOtherControllerOFMsgs);
            ArrayList<String> switches = sortedSwitches.get(mostController.getControllerId());
            if (switches.size() == 0) {
                otherControllers.remove(mostController);
                estimatedOtherControllerOFMsgs.remove(mostController);
                continue;
            }

            for (int index = 0; index < switches.size(); index++) {
                String dpid = switches.get(index);
                long tmpSwitchOFMsgs = mastership.getNumOFMsgsForSingleSwitchInMasterController(mostController, dpid, state);
                long tmpMostControllerOFMsgs = estimatedOtherControllerOFMsgs.get(mostController);
                long tmpTargetControllerOFMsgsChanged = targetControllerOFMsgs + tmpSwitchOFMsgs;
                long tmpMostControllerOFMsgsChanged = tmpMostControllerOFMsgs - tmpSwitchOFMsgs;

                if (tmpMostControllerOFMsgsChanged > avgOFMsgs && tmpTargetControllerOFMsgsChanged < avgOFMsgs) {
                    switches.remove(dpid);
                    topology.get(targetController.getControllerId()).add(dpid);
                    targetControllerOFMsgs = tmpTargetControllerOFMsgsChanged;
                    estimatedOtherControllerOFMsgs.replace(mostController, tmpMostControllerOFMsgsChanged);
                    break;
                }

                if (index == (switches.size() - 1)) {
                    // debugging
                    System.out.println("Estimated OFMsgs: " + estimatedOtherControllerOFMsgs.get(mostController) + " for " + mostController.getControllerId());

                    otherControllers.remove(mostController);
                    estimatedOtherControllerOFMsgs.remove(mostController);
                }
            }
        }

        // debugging
        System.out.println("Estimated OFMsgs: " + targetControllerOFMsgs + " for targetController " + targetController.getControllerId());
        System.out.println("Average target OFMsgs: " + avgOFMsgs);

        mastership.changeMultipleMastership(topology);

    }

    public void distributeMastershipForScaleOutElastiCon(ControllerBean targetController, State state) {

        HashMap<String, ArrayList<String>> topology = new HashMap<>();
        // init topology
        topology.put(targetController.getControllerId(), new ArrayList<>());
        CPManMastership mastership = new CPManMastership();

        HashMap<ControllerBean, Double> removedCPULoads = new HashMap<>();
        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();
        ArrayList<ControllerBean> tmpActiveControllers = mastership.getActiveControllers();

        // init previous topology and removedCPULoad
        for (ControllerBean controller : activeControllers) {
            removedCPULoads.put(controller, 0.0);
        }

        ArrayList<String> sortedControllers = new ArrayList<>();
        while (tmpActiveControllers.size() != 0) {
            ControllerBean tmpController = getHighestCPULoadController(state, tmpActiveControllers, removedCPULoads);
            sortedControllers.add(tmpController.getControllerId());
            tmpActiveControllers.remove(tmpController);
        }

        sortedControllers.remove(targetController.getControllerId());

        double targetAvgCPULoad = averageCPUloadWithTargetController(state, targetController);
        double targetCPULoad = state.getComputingResourceTuples().get(targetController.getControllerId()).avgCpuUsage();
        double targetCPUNormalizeFactor = 40/targetController.getNumCPUs();
        targetCPULoad = targetCPUNormalizeFactor * targetCPULoad;

        if (targetCPULoad > 100.0) {
            targetCPULoad = 100;
        }

        for (String controllerId : sortedControllers) {

            //ArrayList<String> dpids = state.getMastershipTuples().get(controllerId).getSwitchList();
            ArrayList<String> dpids = mastership.getSortedSwitchList(Configuration.getInstance().getControllerBeanWithId(controllerId), state);
            double tmpCPULoad = state.getComputingResourceTuples().get(controllerId).avgCpuUsage();
            double tmpCPUNormalizeFactor = 40/Configuration.getInstance().getControllerBeanWithId(controllerId).getNumCPUs();
            tmpCPULoad = tmpCPUNormalizeFactor * tmpCPULoad;

            if (tmpCPULoad > 100.0) {
                tmpCPULoad = 100;
            }

            // debugging
            //System.out.println("CPU/Switches : " + tmpCPULoadEachSwitch + " for " + controllerId);

            if (dpids.size() == 0) {
                continue;
            }

            double tmpTotalControllerOFMsgs = (double) mastership.getNumOFMsgsForSingleController(Configuration.getInstance().getControllerBeanWithId(controllerId), state);

            for (String dpid : dpids) {

                double tmpSwitchOFMsgs = (double) mastership.getNumOFMsgsForSingleSwitchInMasterController(Configuration.getInstance().getControllerBeanWithId(controllerId), dpid, state);
                double tmpFraction = tmpSwitchOFMsgs / tmpTotalControllerOFMsgs;
                double tmpCPULoadEachSwitch = tmpCPULoad * tmpFraction;

                if (tmpCPULoad - tmpCPULoadEachSwitch > targetAvgCPULoad &&
                        targetCPULoad + tmpCPULoadEachSwitch <= targetAvgCPULoad) {
                    targetCPULoad += tmpCPULoadEachSwitch;
                    tmpCPULoad -= tmpCPULoadEachSwitch;
                    topology.get(targetController.getControllerId()).add(dpid);
                }
            }

            // debugging
            System.out.println("Estimated CPU Load: " + tmpCPULoad + " for " + controllerId);

        }

        // debugging
        System.out.println("Estimated CPU Load: " + targetCPULoad + " for targetController, " + targetController.getControllerId());
        System.out.println("Average target CPU Load: " + targetAvgCPULoad);

        // debugging code
        System.out.println("Topology");
        for (String controllerId : topology.keySet()) {
            System.out.print(controllerId + ": ");
            for (String dpid : topology.get(controllerId)) {
                System.out.print(dpid + " ");
            }
            System.out.println();
        }

        mastership.changeMultipleMastership(topology);
    }

    public void switchOffControllerForScaleIn(ControllerBean targetController, State state) {

        PMBean pm = Configuration.getInstance().getPMBean(DEV_MACHINE_IP_ADDR);

        RESTConnection restConn = new RESTConnection();
        SSHConnection sshConn = new SSHConnection();

        // Remove target controller from OVS
        removeControllerToOVS(targetController, state);

//        String url = RESTURL_DOSCALEIN.replace("<controllerID>", targetController.getControllerId());
//
//        try {
//            restConn.sendCommandToUser(targetController, url);
//            Thread.sleep(3000);
//        } catch (BadRequestException e) {
//            System.out.println("BadRequestException");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        String serviceStopCMD = CMD_ONOS_SERVICE_STOP.replace("<controllerID>", targetController.getControllerId());
        sshConn.sendCommandToUser(pm, serviceStopCMD);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void switchOnControllerForScaleOut(ControllerBean targetController, State state) {

        PMBean pm = Configuration.getInstance().getPMBean(DEV_MACHINE_IP_ADDR);

        RESTConnection restConn = new RESTConnection();
        SSHConnection sshConn = new SSHConnection();

        String serviceStartCMD = CMD_ONOS_SERVICE_START.replace("<controllerID>", targetController.getControllerId());
        String checkServiceCMD = CMD_CHECK_ONOS_SERVICE.replace("<controllerID>", targetController.getControllerId());
        sshConn.sendCommandToUser(pm, serviceStartCMD);
        sshConn.sendCommandToUser(pm, checkServiceCMD);
        System.out.println("ON!");

//        String url = RESTURL_DOSCALEOUT.replace("<controllerID>", targetController.getControllerId());
//        try {
//            Thread.sleep(6000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            restConn.sendCommandToUser(targetController, url);
//            Thread.sleep(3000);
//        } catch (BadRequestException e) {
//            System.out.println("BadRequestException");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Add target controller from OVS
        addControllerToOVS(targetController, state);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void switchOffVMForScaleIn(ControllerBean targetController, State state) {
        PMBean pm = Configuration.getInstance().getPMBean(DEV_MACHINE_IP_ADDR);
        SSHConnection sshConn = new SSHConnection();

        String cmd = CMD_PAUSE_VM.replace("<controllerName>", targetController.getName());
        sshConn.sendCommandToUser(pm, cmd);

    }

    public void switchOnVMForScaleOut(ControllerBean targetController, State state) {
        PMBean pm = Configuration.getInstance().getPMBean(DEV_MACHINE_IP_ADDR);
        SSHConnection sshConn = new SSHConnection();

        String cmd = CMD_RESUME_VM.replace("<controllerName>", targetController.getName());
        sshConn.sendCommandToUser(pm, cmd);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void removeControllerToOVS(ControllerBean targetController, State state) {
        ArrayList<PMBean> mininetMachines = new ArrayList<>();
        CPManMastership mastership = new CPManMastership();
        SSHConnection sshConn = new SSHConnection();

        for (int index = 1; index <= NUM_MININET_MACHINE; index++) {
            String tmpIp = "192.168.200.20" + String.valueOf(index);
            mininetMachines.add(Configuration.getInstance().getMininetMachines().get(tmpIp));
        }

        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();
        activeControllers.remove(targetController);
        String controllerLists = " ";

        for (ControllerBean controller : activeControllers) {
            String tmpController = "tcp:" + controller.getControllerId() + ":6653";
            controllerLists = controllerLists + tmpController + " ";
        }

        String cmd = CMD_SET_CONTROLLER.replace("<controllerIDs>", controllerLists);
        ArrayList<Thread> threads = new ArrayList<>();


        for (PMBean mininetPM : mininetMachines) {
            String totalCmd = " ";
            ArrayList<MininetBean> swes = Configuration.getInstance().getMininets().get(mininetPM.getIpAddr());
            for (MininetBean sw : swes) {
                totalCmd = totalCmd + cmd.replace("<switchID>", sw.getId()) + "&&";
            }
            totalCmd = totalCmd + "echo end";

            ThreadRunRootSSH runObj = new ThreadRunRootSSH(totalCmd, mininetPM);
            Thread thread = new Thread(runObj);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    public void addControllerToOVS(ControllerBean targetController, State state) {
        ArrayList<PMBean> mininetMachines = new ArrayList<>();
        CPManMastership mastership = new CPManMastership();
        SSHConnection sshConn = new SSHConnection();

        for (int index = 1; index <= NUM_MININET_MACHINE; index++) {
            String tmpIp = "192.168.200.20" + String.valueOf(index);
            mininetMachines.add(Configuration.getInstance().getMininetMachines().get(tmpIp));
        }

        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();
        activeControllers.add(targetController);
        String controllerLists = " ";

        for (ControllerBean controller : activeControllers) {
            String tmpController = "tcp:" + controller.getControllerId() + ":6653";
            controllerLists = controllerLists + tmpController + " ";
        }

        String cmd = CMD_SET_CONTROLLER.replace("<controllerIDs>", controllerLists);
        ArrayList<Thread> threads = new ArrayList<>();

        for (PMBean mininetPM : mininetMachines) {
            String totalCmd = " ";
            ArrayList<MininetBean> swes = Configuration.getInstance().getMininets().get(mininetPM.getIpAddr());
            for (MininetBean sw : swes) {
                totalCmd = totalCmd + cmd.replace("<switchID>", sw.getId()) + "&&";
            }
            totalCmd = totalCmd + "echo end";

            ThreadRunRootSSH runObj = new ThreadRunRootSSH(totalCmd, mininetPM);
            Thread thread = new Thread(runObj);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
}

class L1TargetControllerSanityException extends RuntimeException {
    public L1TargetControllerSanityException() {
    }

    public L1TargetControllerSanityException(String message) {
        super(message);
    }
}

class TurnOffFixedControllerException extends RuntimeException {
    public TurnOffFixedControllerException() {
    }

    public TurnOffFixedControllerException(String message) {
        super(message);
    }
}

class ThreadRunRESTAPI implements Runnable {

    String url;
    ControllerBean targetController;

    public ThreadRunRESTAPI(String url, ControllerBean targetController) {
        this.url = url;
        this.targetController = targetController;
    }

    @Override
    public void run() {

        try {
            RESTConnection restConn = new RESTConnection();
            restConn.sendCommandToUser(targetController, url);
            Thread.sleep(3000);
        } catch (BadRequestException e) {
            System.out.println("BadRequestException");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class ThreadRunRootSSH implements Runnable {
    String cmd;
    PMBean pm;

    public ThreadRunRootSSH(String cmd, PMBean pm) {
        this.cmd = cmd;
        this.pm = pm;
    }

    @Override
    public void run() {
        SSHConnection sshConn = new SSHConnection();
        sshConn.sendCommandToRoot(pm, cmd);
    }
}