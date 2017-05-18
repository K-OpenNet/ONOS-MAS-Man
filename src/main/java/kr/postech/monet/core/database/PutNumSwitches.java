package kr.postech.monet.core.database;

import kr.postech.monet.config.GeneralConf;
import kr.postech.monet.config.bean.PMBean;
import kr.postech.monet.config.bean.SiteBean;
import kr.postech.monet.config.bean.VMBean;
import kr.postech.monet.core.monitor.GettingTopology;
import kr.postech.monet.utils.SSHConnectionUtil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by woojoong on 2017-05-18.
 */
public class PutNumSwitches {
    private final String cmdPutNumSwitches = "curl -i -XPOST http://localhost:8086/write?db=kict" +
            " --data-binary \"numswitch,site=<sitename>,pm=<pmname>,vm=<vmname> value=<num_switches> <time>\"";

    public PutNumSwitches() {

    }

    public String getCmdPutNumSwitches() {
        return cmdPutNumSwitches;
    }

    public void putNumSwitchesInDBForAllSites() {

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
                    String tmpCommand = getCmdPutNumSwitches().replace("<sitename>", tmpSite.getSiteAlias());
                    tmpCommand = tmpCommand.replace("<pmname>", tmpPm.getPmAlias());
                    tmpCommand = tmpCommand.replace("<vmname>", tmpVm.getVmAlias());
                    tmpCommand = tmpCommand.replace("<time>", time);

                    Thread tmpThread = new PutNumSwitchesThreadEachVM(tmpVm, tmpCommand);
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

class PutNumSwitchesThreadEachVM extends Thread {

    private VMBean tmpVm;
    private String tmpCommand;

    public PutNumSwitchesThreadEachVM(VMBean tmpVM, String command) {
        this.tmpVm = tmpVM;
        this.tmpCommand = command;
    }

    @Override
    public void run() {
        // Getting # switches of tmpVM
        GettingTopology topo = new GettingTopology();
        List<String> swList = new CopyOnWriteArrayList<String>();
        if(tmpVm.isAlive()) {
            swList = topo.getMasterRoleSwitches(tmpVm);
        }

        // input num of switches in query
        SSHConnectionUtil sshConn = new SSHConnectionUtil();
        tmpCommand = tmpCommand.replace("<num_switches>", String.valueOf(swList.size()));
        sshConn.sendCmdToSingleVM(GeneralConf.dbBean, tmpCommand, 4096);
        //System.out.println(tmpCommand);
    }
}
