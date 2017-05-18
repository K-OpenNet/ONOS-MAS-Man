package kr.postech.monet.config.bean;

import kr.postech.monet.config.pool.PMConfPool;

import java.util.List;

/**
 * Created by woojoong on 2017-05-18.
 */
public class SiteBean {
    private String siteAlias;
    private PMConfPool pmConfPool;

    public SiteBean(String siteAlias, PMConfPool pmConfPool) {
        this.siteAlias = siteAlias;
        this.pmConfPool = pmConfPool;
    }

    public SiteBean(String siteAlias, List<PMBean> pmBeans) {
        this.siteAlias = siteAlias;
        this.pmConfPool = new PMConfPool();
        this.pmConfPool.setPmBeans(pmBeans);
    }

    public String getSiteAlias() {
        return siteAlias;
    }

    public void setSiteAlias(String siteAlias) {
        this.siteAlias = siteAlias;
    }

    public PMConfPool getPmConfPool() {
        return pmConfPool;
    }

    public void setPmConfPool(PMConfPool pmConfPool) {
        this.pmConfPool = pmConfPool;
    }
}
