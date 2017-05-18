package kr.postech.monet.core.database;

import kr.postech.monet.config.GeneralConf;
import kr.postech.monet.config.bean.PMBean;
import kr.postech.monet.config.bean.SWBean;
import kr.postech.monet.config.bean.SiteBean;
import kr.postech.monet.config.bean.VMBean;
import kr.postech.monet.core.monitor.GettingCPTraffic;
import kr.postech.monet.core.monitor.GettingTopology;
import kr.postech.monet.utils.SSHConnectionUtil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by woojoong on 2017-05-18.
 */
public class PutControlTraffic {    private final String cmdPutControltraffic = "curl -i -XPOST http://localhost:8086/write?db=kict" +
        " --data-binary \"controltraffic,site=<sitename>,pm=<pmname>,vm=<vmname>,inbound=<num_inbound>," +
        "outbound=<num_outbound>,flowmod=<num_flowmod>,flowrem=<num_flowrem>,statreq=<num_statreq>," +
        "statrep=<num_statrep> value=<total_packets> <time>\"";

    public PutControlTraffic() {

    }

    public String getCmdPutControltraffic() {
        return cmdPutControltraffic;
    }

    public void putControlTrafficInfoInDBForAllSites() {

        Date date = new Date();
        String time = String.valueOf(date.getTime()) + "000000";

        List<SiteBean> siteBeanList = GeneralConf.siteConfPoolList.getSiteBeans();
        List<Thread> threadPoolForVM = new CopyOnWriteArrayList<Thread>();

        for (int index1 = 0; index1 < siteBeanList.size(); index1++) {
            SiteBean tmpSite = siteBeanList.get(index1);
            List<PMBean> pmBeanList = siteBeanList.get(index1).getPmConfPool().getPmBeans();
            for (int index2 = 0; index2 < pmBeanList.size(); index2++) {
                PMBean tmpPm = pmBeanList.get(index2);
                List<VMBean> vmBeanList = pmBeanList.get(index2).getVmConfPool().getVmBeans();
                for (int index3 = 0; index3 < vmBeanList.size(); index3++) {
                    VMBean tmpVm = vmBeanList.get(index3);
                    String tmpCommand = getCmdPutControltraffic().replace("<sitename>", tmpSite.getSiteAlias());
                    tmpCommand = tmpCommand.replace("<pmname>", tmpPm.getPmAlias());
                    tmpCommand = tmpCommand.replace("<vmname>", tmpVm.getVmAlias());
                    tmpCommand = tmpCommand.replace("<time>", time);

                    Thread tmpThread = new PutControlTrafficThreadEachVM(tmpVm, tmpCommand);
                    threadPoolForVM.add(tmpThread);
                    tmpThread.run();
                }
            }
        }

        for (int index1 = 0; index1 < threadPoolForVM.size(); index1++) {
            Thread tmpThread = threadPoolForVM.get(index1);
            try {
                tmpThread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class PutControlTrafficThreadEachVM extends Thread {
    private VMBean tmpVm;
    private String tmpCommand;

    public PutControlTrafficThreadEachVM(VMBean tmpVm, String tmpCommand) {
        this.tmpVm = tmpVm;
        this.tmpCommand = tmpCommand;
    }

    @Override
    public void run() {
        // Getting # switches of tmpVM
        SSHConnectionUtil sshConn = new SSHConnectionUtil();

        GettingTopology topo = new GettingTopology();
        GettingCPTraffic cpTraffic = new GettingCPTraffic();
        List<SWBean> swList = new CopyOnWriteArrayList<SWBean>();
        int numInbound = 0, numOutbound = 0, numFlowmod = 0, numFlowrem = 0, numStatreq = 0, numStatrep = 0;

        if(tmpVm.isAlive()) {
            swList = topo.getSwitches(tmpVm);
            cpTraffic.getCPTraffic(tmpVm, swList);
            for (int index4 = 0; index4 < swList.size(); index4++) {
                numInbound = numInbound + swList.get(index4).getInboundPackets();
                numOutbound = numOutbound + swList.get(index4).getOutboundPackets();
                numFlowmod = numFlowmod + swList.get(index4).getFlowModPackets();
                numFlowrem = numFlowrem + swList.get(index4).getFlowRemovePackets();
                numStatreq = numStatreq + swList.get(index4).getStatRequestPackets();
                numStatrep = numStatrep + swList.get(index4).getStatReplyPackets();
            }
        }

        // input num of switches in query
        tmpCommand = tmpCommand.replace("<num_inbound>", String.valueOf(numInbound));
        tmpCommand = tmpCommand.replace("<num_outbound>", String.valueOf(numOutbound));
        tmpCommand = tmpCommand.replace("<num_flowmod>", String.valueOf(numFlowmod));
        tmpCommand = tmpCommand.replace("<num_flowrem>", String.valueOf(numFlowrem));
        tmpCommand = tmpCommand.replace("<num_statreq>", String.valueOf(numStatreq));
        tmpCommand = tmpCommand.replace("<num_statrep>", String.valueOf(numStatrep));
        tmpCommand = tmpCommand.replace("<total_packets>", String.valueOf(numInbound+numOutbound+numFlowmod+numFlowrem+numStatreq+numStatrep));
        sshConn.sendCmdToSingleVM(GeneralConf.dbBean, tmpCommand, 4096);
        //System.out.println(tmpCommand);
    }
}
