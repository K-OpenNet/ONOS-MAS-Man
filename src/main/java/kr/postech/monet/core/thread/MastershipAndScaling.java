package kr.postech.monet.core.thread;

import kr.postech.monet.config.GeneralConf;
import kr.postech.monet.config.bean.SiteBean;
import kr.postech.monet.core.database.PutCPULoad;
import kr.postech.monet.core.database.PutControlTraffic;
import kr.postech.monet.core.database.PutNumSwitches;
import kr.postech.monet.core.mastership.MastershipCore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by woojoong on 2017-05-18.
 */
public class MastershipAndScaling {

    public static ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

    public static void funcMastershipAndScalingThread() {

        int periodSec = 60;

        final SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS");

        final PutControlTraffic controlTraffic = new PutControlTraffic();
        final PutCPULoad cpuLoad = new PutCPULoad();
        final PutNumSwitches numSwitches = new PutNumSwitches();

        exec.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try{

                    Calendar cal = Calendar.getInstance();
                    System.out.println(format.format(cal.getTime()));
                    controlTraffic.putControlTrafficInfoInDBForAllSites();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                try{
                    cpuLoad.putCPULoadInDBForAllSites();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    List<Thread> threadPool = new CopyOnWriteArrayList<Thread>();
                    for (int index = 0; index < GeneralConf.siteConfPoolList.getSiteBeans().size(); index++) {
                        Thread tmpThread = new ChangeMastershipThread(GeneralConf.siteConfPoolList.getSiteBeans().get(index));
                        threadPool.add(tmpThread);
                        tmpThread.run();
                    }

                    for (int index = 0; index < threadPool.size(); index++) {
                        Thread tmpThread = threadPool.get(index);
                        try {
                            tmpThread.join();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    numSwitches.putNumSwitchesInDBForAllSites();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, periodSec, TimeUnit.SECONDS);
    }
}

class ChangeMastershipThread extends Thread {
    private SiteBean tmpSite;

    public ChangeMastershipThread(SiteBean tmpSite) {
        this.tmpSite = tmpSite;
    }

    @Override
    public void run() {
        MastershipCore mastership = new MastershipCore();
        mastership.forwardGreedyMastership(tmpSite);
    }
}