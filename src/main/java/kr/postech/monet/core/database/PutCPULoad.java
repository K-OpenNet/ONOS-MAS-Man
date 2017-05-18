package kr.postech.monet.core.database;

import kr.postech.monet.config.GeneralConf;
import kr.postech.monet.config.bean.PMBean;
import kr.postech.monet.config.bean.SiteBean;
import kr.postech.monet.config.bean.VMBean;
import kr.postech.monet.core.monitor.GettingCPULoad;
import kr.postech.monet.utils.SSHConnectionUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by woojoong on 2017-05-18.
 */
public class PutCPULoad {
    private final String cmdPutCPULoad = "curl -i -XPOST http://localhost:8086/write?db=kict" +
            " --data-binary \"cpuload,site=<sitename>,pm=<pmname>,vm=<vmname> value=<cpu_load> <time>\"";

    public PutCPULoad() {

    }

    public String getCmdPutCPULoad() {
        return cmdPutCPULoad;
    }

    public void putCPULoadInDBForAllSites() {
        Date date = new Date();
        String time = String.valueOf(date.getTime()) + "000000";

        List<SiteBean> siteBeanList = GeneralConf.siteConfPoolList.getSiteBeans();
        List<Thread> threadPoolForVM = new CopyOnWriteArrayList<Thread>();

        for (int index1 = 0; index1 < siteBeanList.size(); index1++) {
            SiteBean tmpSite = siteBeanList.get(index1);
            List<PMBean> pmBeanList = siteBeanList.get(index1).getPmConfPool().getPmBeans();
            for (int index2 = 0; index2 < pmBeanList.size(); index2++) {
                PMBean tmpPm = pmBeanList.get(index2);

                GettingCPULoad cpuLoad = new GettingCPULoad();
                HashMap<VMBean, Float> results = cpuLoad.getCPULoad(tmpPm);
                List<VMBean> vmBeanList = pmBeanList.get(index2).getVmConfPool().getVmBeans();
                for (int index3 = 0; index3 < vmBeanList.size(); index3++) {
                    VMBean tmpVm = vmBeanList.get(index3);
                    String tmpCommand = getCmdPutCPULoad().replace("<sitename>", tmpSite.getSiteAlias());
                    tmpCommand = tmpCommand.replace("<pmname>", tmpPm.getPmAlias());
                    tmpCommand = tmpCommand.replace("<vmname>", tmpVm.getVmAlias());
                    tmpCommand = tmpCommand.replace("<time>", time);

                    Thread tmpThread = new PutCPULoadThreadEachVM(tmpVm, tmpCommand, results);
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

class PutCPULoadThreadEachVM extends Thread {
    private VMBean tmpVm;
    private String tmpCommand;
    private HashMap<VMBean, Float> results;

    public PutCPULoadThreadEachVM(VMBean tmpVm, String tmpCommand, HashMap<VMBean, Float> results) {
        this.tmpVm = tmpVm;
        this.tmpCommand = tmpCommand;
        this.results = results;
    }

    @Override
    public void run() {
        SSHConnectionUtil sshConn = new SSHConnectionUtil();

        if (results != null && results.containsKey(tmpVm)) {
            tmpCommand = tmpCommand.replace("<cpu_load>", String.valueOf(results.get(tmpVm)));
        } else {
            tmpCommand = tmpCommand.replace("<cpu_load>", "0.0");
        }
        sshConn.sendCmdToSingleVM(GeneralConf.dbBean, tmpCommand, 4096);
        //System.out.println(tmpCommand);
    }
}
