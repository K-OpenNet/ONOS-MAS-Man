package kr.postech.monet.core.mastership;

import com.eclipsesource.json.JsonObject;
import kr.postech.monet.config.bean.PMBean;
import kr.postech.monet.config.bean.SWBean;
import kr.postech.monet.config.bean.SiteBean;
import kr.postech.monet.config.bean.VMBean;
import kr.postech.monet.core.monitor.GettingCPTraffic;
import kr.postech.monet.core.monitor.GettingTopology;
import kr.postech.monet.utils.RESTConnectionUtil;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by woojoong on 2017-05-18.
 */
public class MastershipCore {

    private final String RESTURL_CHANGEMASTERSHIP = "/onos/v1/mastership";

    public MastershipCore() {
    }

    public void forwardGreedyMastership (SiteBean sourceSite) {
        List<PMBean> totalPMList = sourceSite.getPmConfPool().getPmBeans();
        List<VMBean> totalVMList = new CopyOnWriteArrayList<VMBean>();
        HashMap<VMBean, List<SWBean>> overallCPTraffic = new HashMap<VMBean, List<SWBean>>();
        HashMap<VMBean, List<String>> masterRoleSwitches = new HashMap<VMBean, List<String>>();

        HashMap<VMBean, List<SWBean>> movingSwitches = new HashMap<VMBean, List<SWBean>>();
        HashMap<VMBean, List<SWBean>> candidateSwitches = new HashMap<VMBean, List<SWBean>>();

        int averageControlPackets = 0;

        List<VMBean> moreControlPacketsVMList = new CopyOnWriteArrayList<VMBean>();
        List<VMBean> lessControlPacketsVMList = new CopyOnWriteArrayList<VMBean>();

        for (int index1 = 0; index1 < totalPMList.size(); index1++) {
            for (int index2 = 0; index2 < totalPMList.get(index1).getVmConfPool().getVmBeans().size(); index2++) {
                totalVMList.add(totalPMList.get(index1).getVmConfPool().getVmBeans().get(index2));
            }
        }

        // Getting control traffic information and master role information for all VMs.
        GettingCPTraffic getCPTrafficObj = new GettingCPTraffic();
        GettingTopology getTopologyObj = new GettingTopology();
        for (int index = 0; index < totalVMList.size(); index++) {
            List<SWBean> tmpCPTraffic = getCPTrafficObj.getCPTraffic(totalVMList.get(index), getTopologyObj.getSwitches(totalVMList.get(index)));
            overallCPTraffic.put(totalVMList.get(index), tmpCPTraffic);
            List<String> tmpMasterRoleSwitches = getTopologyObj.getMasterRoleSwitches(totalVMList.get(index));
            masterRoleSwitches.put(totalVMList.get(index), tmpMasterRoleSwitches);

            averageControlPackets = averageControlPackets + totalControlPacket(tmpCPTraffic);
        }

        averageControlPackets = averageControlPackets / totalVMList.size();

        // split: VMs including more control packets than total average and others
        for (int index = 0; index < totalVMList.size(); index++) {
            VMBean targetVM = totalVMList.get(index);

            if(totalControlPacket(overallCPTraffic.get(targetVM)) > averageControlPackets) {
                moreControlPacketsVMList.add(targetVM);
            } else {
                lessControlPacketsVMList.add(targetVM);
            }
        }

        // initialize movingSwitches hashmap
        for (int index = 0; index < lessControlPacketsVMList.size(); index++) {
            movingSwitches.put(lessControlPacketsVMList.get(index), new CopyOnWriteArrayList<SWBean>());
        }

        // initialize candidateSwitches hashmap
        for (int index1 = 0; index1 < moreControlPacketsVMList.size(); index1++) {
            candidateSwitches.put(moreControlPacketsVMList.get(index1), new CopyOnWriteArrayList<SWBean>());
            List<SWBean> tmpSWBeanAllCP = overallCPTraffic.get(moreControlPacketsVMList.get(index1));
            List<String> tmpMasterSWList = masterRoleSwitches.get(moreControlPacketsVMList.get(index1));
            Set<String> tmpMasterSWSet = new CopyOnWriteArraySet<String>();
            for (int index2 = 0; index2 < tmpMasterSWList.size(); index2++) {
                tmpMasterSWSet.add(tmpMasterSWList.get(index2));
            }
            for (int index2 = 0; index2 < tmpSWBeanAllCP.size(); index2++) {
                if (tmpMasterSWSet.contains(tmpSWBeanAllCP.get(index2).getDpid())) {
                    candidateSwitches.get(moreControlPacketsVMList.get(index1)).add(tmpSWBeanAllCP.get(index2));
                }
            }
        }

        // ascending sorting for SWBeanLists
        for (int index = 0; index < moreControlPacketsVMList.size(); index++) {
            Collections.shuffle(candidateSwitches.get(moreControlPacketsVMList.get(index)));
        }

        // pick master role switch for each controller which receives control packets below average
        List<VMBean> asOrderedVMforMastership = asOrderedVMforMastership(candidateSwitches);
        int numControlPackets = 0;
        for (int index1 = 0; index1 < asOrderedVMforMastership.size(); index1++) {
            VMBean tmpVM = asOrderedVMforMastership.get(index1);
            numControlPackets = totalControlPacket(overallCPTraffic.get(tmpVM));
            for (int index2 = 0; index2 < lessControlPacketsVMList.size(); index2++) {
                for (int index3 = 0; index3 < candidateSwitches.get(tmpVM).size(); index3++) {
                    SWBean tmpSW = candidateSwitches.get(tmpVM).get(index3);
                    if (numControlPackets - tmpSW.getTotalControlPackets() > averageControlPackets &&
                            totalControlPacket(overallCPTraffic.get(lessControlPacketsVMList.get(index2)))
                                    + tmpSW.getTotalControlPackets()
                                    + totalControlPacket(movingSwitches.get(lessControlPacketsVMList.get(index2))) < averageControlPackets) {
                        movingSwitches.get(lessControlPacketsVMList.get(index2)).add(tmpSW);
                        numControlPackets = numControlPackets - tmpSW.getTotalControlPackets();
                        candidateSwitches.get(tmpVM).remove(tmpSW);
                    }
                }
            }
            numControlPackets = 0;
        }

        // change master role
        for (int index1 = 0; index1 < lessControlPacketsVMList.size(); index1++) {
            for (int index2 = 0; index2 < movingSwitches.get(lessControlPacketsVMList.get(index1)).size(); index2++) {
                changeMastership(movingSwitches.get(lessControlPacketsVMList.get(index1)).get(index2), lessControlPacketsVMList.get(index1));
            }
        }

        System.out.println("Finish Mastership for this Site");
    }

    public void changeMastership (SWBean movingSW, VMBean targetVM) {
        String mastershipRESTURL = "http://" + targetVM.getAccessIPAddress() + ":" + targetVM.getAccessHTTPPort() + RESTURL_CHANGEMASTERSHIP;

        JsonObject rootObj = new JsonObject();
        rootObj.add("deviceId", movingSW.getDpid());
        rootObj.add("nodeId", targetVM.getIpAddress());
        rootObj.add("role", "MASTER");

        RESTConnectionUtil restConn = new RESTConnectionUtil();
        restConn.putRESTToSingleVM(targetVM, mastershipRESTURL, rootObj);

    }

    public int totalControlPacket (List<SWBean> sourceSwitches) {

        int results = 0;

        for (int index = 0; index < sourceSwitches.size(); index++) {
            results = results + sourceSwitches.get(index).getTotalControlPackets();
        }

        return results;
    }

    public VMBean highestControlPacketVM (HashMap<VMBean, List<SWBean>> sourceHashMap) {
        Set<VMBean> vmSet = sourceHashMap.keySet();
        Iterator<VMBean> itVMSet = vmSet.iterator();

        if(!itVMSet.hasNext()) {
            return null;
        }

        VMBean highestControlPacketVM = itVMSet.next();
        while (itVMSet.hasNext()) {
            VMBean tempVM = itVMSet.next();
            if (totalControlPacket(sourceHashMap.get(tempVM)) > totalControlPacket(sourceHashMap.get(highestControlPacketVM))) {
                highestControlPacketVM = tempVM;
            }
        }

        return highestControlPacketVM;
    }

    public List<VMBean> asOrderedVMforMastership (HashMap<VMBean, List<SWBean>> sourceHashMap) {
        HashMap<VMBean, List<SWBean>> tmpHashMap = (HashMap<VMBean, List<SWBean>>) sourceHashMap.clone();
        List<VMBean> resultList = new CopyOnWriteArrayList<VMBean>();

        for (int index = 0; index < tmpHashMap.size(); index++) {

            VMBean tmpVMBean = highestControlPacketVM(tmpHashMap);
            resultList.add(tmpVMBean);
            tmpHashMap.remove(tmpVMBean);
        }
        return resultList;
    }


}

/**
 * For sorting
 */
class AscendingComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        int totalControlPackets1 = ((SWBean) o1).getTotalControlPackets();
        int totalControlPackets2 = ((SWBean) o2).getTotalControlPackets();
        return totalControlPackets1 < totalControlPackets2 ? -1 : (totalControlPackets1 == totalControlPackets2 ? 0 : 1);
    }
}
