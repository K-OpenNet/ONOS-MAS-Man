package Scaling;

import Beans.ControllerBean;
import Beans.PMBean;
import Database.Configure.Configuration;
import Database.Tables.State;
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
        if (DECISIONMAKER_TYPE == DecisionMaker.decisionMakerType.SCALING_CPU) {
            distributeMastershipForScaleInElastiCon(targetController, state);
        } else {
            distributeMastershipForScaleInAES(targetController, state);
        }
        targetController.setActive(false);
    }

    public void runL2ONOSScaleIn(ControllerBean targetController, State state) {
        targetController.setOnosAlive(false);
        runL1ONOSScaleIn(targetController, state);
        switchOffControllerForScaleIn(targetController, state);
    }

    public void runL3ONOSScaleIn(ControllerBean targetController, State state) {
        targetController.setVmAlive(false);
        runL2ONOSScaleIn(targetController, state);
        switchOffVMForScaleIn(targetController, state);
    }

    public void runL1ONOSScaleOut(ControllerBean targetController, State state) {
        targetController.setActive(true);
        distributeMastershipForScaleOut(targetController, state);
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

    public void distributeMastershipForScaleInElastiCon(ControllerBean targetController, State state) {

        CPManMastership mastership = new CPManMastership();
        ArrayList<ControllerBean> activeControllers = mastership.getActiveControllers();
        activeControllers.remove(targetController);
        int numSwitchesInTarget = state.getMastershipTuples().get(targetController.getBeanKey()).getSwitchList().size();
        double cpuLoadForEachSwitch = state.getComputingResourceTuples().get(targetController.getBeanKey()).avgCpuUsage()/numSwitchesInTarget;
        ArrayList<String> masterSwitchListInTargetController = (ArrayList<String>) state.getMastershipTuples().get(targetController.getBeanKey()).getSwitchList().clone();

        HashMap<ControllerBean, Double> estimatedControllersCPULoad = new HashMap<>();
        for (ControllerBean controller : activeControllers) {
            estimatedControllersCPULoad.put(controller, state.getComputingResourceTuples().get(controller.getBeanKey()).avgCpuUsage());
        }

        HashMap<String, ArrayList<String>> topology = new HashMap<>();
        for (ControllerBean controller : activeControllers) {
            topology.putIfAbsent(controller.getControllerId(), new ArrayList<>());
        }

        for (int index = 0; index < numSwitchesInTarget; index++) {
            ControllerBean lowestCPULoadController = getLowestCPULoadController(estimatedControllersCPULoad);
            double tmpCPULoad = estimatedControllersCPULoad.get(lowestCPULoadController) + cpuLoadForEachSwitch;
            estimatedControllersCPULoad.replace(lowestCPULoadController, tmpCPULoad);
            System.out.println(masterSwitchListInTargetController.size());
            topology.get(lowestCPULoadController.getControllerId()).add(masterSwitchListInTargetController.get(index));
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

    public void distributeMastershipForScaleOut(ControllerBean targetController, State state) {
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
                    otherControllers.remove(mostController);
                    estimatedOtherControllerOFMsgs.remove(mostController);
                }
            }
        }

        mastership.changeMultipleMastership(topology);

    }

    public void switchOffControllerForScaleIn(ControllerBean targetController, State state) {
        PMBean pm = Configuration.getInstance().getPMBean(DEV_MACHINE_IP_ADDR);

        RESTConnection restConn = new RESTConnection();
        SSHConnection sshConn = new SSHConnection();

        String url = RESTURL_DOSCALEIN.replace("<controllerID>", targetController.getControllerId());

        ArrayList<Thread> threads = new ArrayList<>();

        ThreadRunRESTAPI mainRunnableObj = new ThreadRunRESTAPI(url, targetController);
        Thread mainThread = new Thread(mainRunnableObj);
        threads.add(mainThread);

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            if (!controller.isActive()) {
                continue;
            }

            ThreadRunRESTAPI runnableObj = new ThreadRunRESTAPI(url, controller);
            Thread thread = new Thread(runnableObj);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.run();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String serviceStopCMD = CMD_ONOS_SERVICE_STOP.replace("<controllerID>", targetController.getControllerId());
        sshConn.sendCommandToUser(pm, serviceStopCMD);
    }

    public void switchOnControllerForScaleOut(ControllerBean targetController, State state) {
        PMBean pm = Configuration.getInstance().getPMBean(DEV_MACHINE_IP_ADDR);

        RESTConnection restConn = new RESTConnection();
        SSHConnection sshConn = new SSHConnection();

        String serviceStartCMD = CMD_ONOS_SERVICE_START.replace("<controllerID>", targetController.getControllerId());
        String checkServiceCMD = CMD_CHECK_ONOS_SERVICE.replace("<controllerID>", targetController.getControllerId());
        sshConn.sendCommandToUser(pm, serviceStartCMD);
        System.out.println(sshConn.sendCommandToUser(pm, checkServiceCMD));

        String url = RESTURL_DOSCALEOUT.replace("<controllerID>", targetController.getControllerId());

        ArrayList<Thread> threads = new ArrayList<>();

        ThreadRunRESTAPI mainRunnableObj = new ThreadRunRESTAPI(url, targetController);
        Thread mainThread = new Thread(mainRunnableObj);
        threads.add(mainThread);

        for (ControllerBean controller : Configuration.getInstance().getControllers()) {
            if (!controller.isActive()) {
                continue;
            }

            ThreadRunRESTAPI runnableObj = new ThreadRunRESTAPI(url, controller);
            Thread thread = new Thread(runnableObj);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.run();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
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