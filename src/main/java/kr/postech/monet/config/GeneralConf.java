package kr.postech.monet.config;

import kr.postech.monet.config.bean.PMBean;
import kr.postech.monet.config.bean.SiteBean;
import kr.postech.monet.config.bean.VMBean;
import kr.postech.monet.config.pool.SiteConfPool;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by woojoong on 2017-05-18.
 */
public class GeneralConf {
    // System parameters
    public static final String REST_URL_POSTFIX = "/onos/v1";

    // Pool list for PMConfPool
    public static SiteConfPool siteConfPoolList;
    public static VMBean dbBean;

    public GeneralConf() {
        System.out.println("Initialize orchestrator!");
        siteConfPoolList = new SiteConfPool();
/*
        //DB
        dbBean = new VMBean("----");
        dbBean.setIpAddress("***.***.***.***");
        dbBean.setSshPort("22");
        dbBean.setHttpPort("8080");
        dbBean.setAccessIPAddress("***.***.***.***");
        //dbBean.setAccessIPAddress("***.***.***.***");
        dbBean.setAccessSSHPort("*****");
        dbBean.setAccessHTTPPort("*****");
        dbBean.setID("----");
        dbBean.setPW("*******");

        //POSTECH - two sites
        //POSTECH - KOREN K-BOX
        VMBean vmONOS1_POSTECH1 = new VMBean("----");
        vmONOS1_POSTECH1.setIpAddress("***.***.***.***");
        vmONOS1_POSTECH1.setSshPort("22");
        vmONOS1_POSTECH1.setHttpPort("8181");
        //vmONOS1_POSTECH1.setAccessIPAddress("***.***.***.***");
        vmONOS1_POSTECH1.setAccessIPAddress("***.***.***.***");
        vmONOS1_POSTECH1.setAccessSSHPort("****");
        vmONOS1_POSTECH1.setAccessHTTPPort("****");
        vmONOS1_POSTECH1.setID("***");
        vmONOS1_POSTECH1.setPW("***");
        vmONOS1_POSTECH1.setONOSID("***");
        vmONOS1_POSTECH1.setONOSPW("***");
        VMBean vmONOS2_POSTECH1 = new VMBean("ONOS-2-POSTECH1");
        vmONOS2_POSTECH1.setIpAddress("***.***.***.***");
        vmONOS2_POSTECH1.setSshPort("22");
        vmONOS2_POSTECH1.setHttpPort("8181");
        //vmONOS2_POSTECH1.setAccessIPAddress("***.***.***.***");
        vmONOS2_POSTECH1.setAccessIPAddress("***.***.***.***");
        vmONOS2_POSTECH1.setAccessSSHPort("***");
        vmONOS2_POSTECH1.setAccessHTTPPort("***");
        vmONOS2_POSTECH1.setID("***");
        vmONOS2_POSTECH1.setPW("***");
        vmONOS2_POSTECH1.setONOSID("***");
        vmONOS2_POSTECH1.setONOSPW("***");
        VMBean vmONOS3_POSTECH1 = new VMBean("ONOS-3-POSTECH1");
        vmONOS3_POSTECH1.setIpAddress("***.***.***.***");
        vmONOS3_POSTECH1.setSshPort("22");
        vmONOS3_POSTECH1.setHttpPort("8181");
        //vmONOS3_POSTECH1.setAccessIPAddress("***.***.***.***");
        vmONOS3_POSTECH1.setAccessIPAddress("***.***.***.***");
        vmONOS3_POSTECH1.setAccessSSHPort("***");
        vmONOS3_POSTECH1.setAccessHTTPPort("***");
        vmONOS3_POSTECH1.setID("***");
        vmONOS3_POSTECH1.setPW("***");
        vmONOS3_POSTECH1.setONOSID("***");
        vmONOS3_POSTECH1.setONOSPW("***");

        List<VMBean> vmONOSList_POSTECH1 = new CopyOnWriteArrayList<VMBean>();
        vmONOSList_POSTECH1.add(vmONOS1_POSTECH1);
        vmONOSList_POSTECH1.add(vmONOS2_POSTECH1);
        vmONOSList_POSTECH1.add(vmONOS3_POSTECH1);
        PMBean pm_POSTECH1 = new PMBean("PM-POSTECH1", vmONOSList_POSTECH1);
        pm_POSTECH1.setID("***");
        pm_POSTECH1.setPW("***");
        pm_POSTECH1.setSshPort("***");
        pm_POSTECH1.setAccessSSHPort("***");
        pm_POSTECH1.setIpAddress("***.***.***.***");
        //pm_POSTECH1.setAccessIPAddress("***.***.***.***");
        pm_POSTECH1.setAccessIPAddress("***.***.***.***");
        pm_POSTECH1.setNumCPU(32);

        List<PMBean> pmList_POSTECH1 = new CopyOnWriteArrayList<PMBean>();
        pmList_POSTECH1.add(pm_POSTECH1);
        SiteBean site_POSTECH1 = new SiteBean("POSTECH-1", pmList_POSTECH1);

        //POSTECH - MoNet
        VMBean vmONOS1_POSTECH2 = new VMBean("onos-1");
        vmONOS1_POSTECH2.setIpAddress("***.***.***.***");
        vmONOS1_POSTECH2.setSshPort("22");
        vmONOS1_POSTECH2.setHttpPort("8181");
        vmONOS1_POSTECH2.setAccessIPAddress("***.***.***.***");
        vmONOS1_POSTECH2.setAccessSSHPort("***");
        vmONOS1_POSTECH2.setAccessHTTPPort("***");
        vmONOS1_POSTECH2.setID("***");
        vmONOS1_POSTECH2.setPW("***");
        vmONOS1_POSTECH2.setONOSID("***");
        vmONOS1_POSTECH2.setONOSPW("***");
        VMBean vmONOS2_POSTECH2 = new VMBean("onos-2");
        vmONOS2_POSTECH2.setIpAddress("***.***.***.***");
        vmONOS2_POSTECH2.setSshPort("22");
        vmONOS2_POSTECH2.setHttpPort("8181");
        vmONOS2_POSTECH2.setAccessIPAddress("***.***.***.***");
        vmONOS2_POSTECH2.setAccessSSHPort("***");
        vmONOS2_POSTECH2.setAccessHTTPPort("***");
        vmONOS2_POSTECH2.setID("***");
        vmONOS2_POSTECH2.setPW("***");
        vmONOS2_POSTECH2.setONOSID("***");
        vmONOS2_POSTECH2.setONOSPW("***");
        VMBean vmONOS3_POSTECH2 = new VMBean("onos-3");
        vmONOS3_POSTECH2.setIpAddress("***.***.***.***");
        vmONOS3_POSTECH2.setSshPort("22");
        vmONOS3_POSTECH2.setHttpPort("8181");
        vmONOS3_POSTECH2.setAccessIPAddress("***.***.***.***");
        vmONOS3_POSTECH2.setAccessSSHPort("***");
        vmONOS3_POSTECH2.setAccessHTTPPort("***");
        vmONOS3_POSTECH2.setID("***");
        vmONOS3_POSTECH2.setPW("***");
        vmONOS3_POSTECH2.setONOSID("***");
        vmONOS3_POSTECH2.setONOSPW("***");

        List<VMBean> vmONOSList_POSTECH2 = new CopyOnWriteArrayList<VMBean>();
        vmONOSList_POSTECH2.add(vmONOS1_POSTECH2);
        vmONOSList_POSTECH2.add(vmONOS2_POSTECH2);
        vmONOSList_POSTECH2.add(vmONOS3_POSTECH2);
        PMBean pm_POSTECH2 = new PMBean("PM-POSTECH2", vmONOSList_POSTECH2);
        pm_POSTECH2.setID("***");
        pm_POSTECH2.setPW("***");
        pm_POSTECH2.setSshPort("***");
        pm_POSTECH2.setAccessSSHPort("***");
        pm_POSTECH2.setIpAddress("***.***.***.***");
        pm_POSTECH2.setAccessIPAddress("***.***.***.***");
        pm_POSTECH2.setNumCPU(40);

        List<PMBean> pmList_POSTECH2 = new CopyOnWriteArrayList<PMBean>();
        pmList_POSTECH2.add(pm_POSTECH2);
        SiteBean site_POSTECH2 = new SiteBean("POSTECH-2", pmList_POSTECH2);

        //make site pool -- add more site
        List<SiteBean> siteList = new CopyOnWriteArrayList<SiteBean>();
        //siteList.add(site_POSTECH1);
        //siteList.add(site_POSTECH2);
        siteConfPoolList.setSiteBeans(siteList);
    }
}
