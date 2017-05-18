package kr.postech.monet.core.thread;

import kr.postech.monet.config.GeneralConf;
import kr.postech.monet.config.bean.PMBean;
import kr.postech.monet.config.bean.SiteBean;
import kr.postech.monet.config.bean.VMBean;
import kr.postech.monet.utils.SSHConnectionUtil;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by woojoong on 2017-05-18.
 */
public class HeartbeatCheckThread {

    public static ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

    public static void funcCheckHeartBeatAllMachinesThread() {
        int periodSec = 10;

        final SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS");

        exec.scheduleAtFixedRate(new Runnable() {
            public void run() {
                //SSHConnectionUtil sshConn = new SSHConnectionUtil();
                //sshConn.checkHeartBeatAllMachines();

                List<Thread> threadPoolForVM = new CopyOnWriteArrayList<Thread>();
                List<Thread> threadPoolForPM = new CopyOnWriteArrayList<Thread>();

                List<SiteBean> siteBeanList = GeneralConf.siteConfPoolList.getSiteBeans();
                for (int index1 = 0; index1 < siteBeanList.size(); index1++) {
                    SiteBean tmpSiteBean = siteBeanList.get(index1);
                    List<PMBean> pmBeanList = tmpSiteBean.getPmConfPool().getPmBeans();
                    for (int index2 = 0; index2 < pmBeanList.size(); index2++) {
                        PMBean tmpPmBean = pmBeanList.get(index2);
                        Thread tmpThreadPM = new HeartbeatCheckThreadEachPM(tmpPmBean);
                        threadPoolForPM.add(tmpThreadPM);
                        tmpThreadPM.start();
                        List<VMBean> vmBeanList = tmpPmBean.getVmConfPool().getVmBeans();
                        for (int index3 = 0; index3 < vmBeanList.size(); index3++) {
                            VMBean tmpVmBean = vmBeanList.get(index3);
                            Thread tmpThreadVM = new HeartbeatCheckThreadEachVM(tmpVmBean);
                            threadPoolForVM.add(tmpThreadVM);
                            tmpThreadVM.start();
                        }
                    }
                }

                for (int index1 = 0; index1 < threadPoolForPM.size(); index1++) {
                    Thread tmpThread = threadPoolForPM.get(index1);
                    try {
                        tmpThread.join();
                    } catch (Exception e) {
                        e.printStackTrace();
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
        }, 0, periodSec, TimeUnit.SECONDS);
    }


}

class HeartbeatCheckThreadEachVM extends Thread {

    private VMBean targetVM;

    public HeartbeatCheckThreadEachVM(VMBean targetVM) {
        this.targetVM = targetVM;
    }

    @Override
    public void run() {
        SSHConnectionUtil sshConn = new SSHConnectionUtil();
        targetVM.setAlive(sshConn.checkHeartBeatToSingleVM(targetVM));
    }
}

class HeartbeatCheckThreadEachPM extends Thread {

    private PMBean targetPM;

    public HeartbeatCheckThreadEachPM(PMBean targetPM) {
        this.targetPM = targetPM;
    }

    @Override
    public void run() {
        SSHConnectionUtil sshConn = new SSHConnectionUtil();
        targetPM.setAlive(sshConn.checkHeartBeatToSinglePM(targetPM));
    }
}