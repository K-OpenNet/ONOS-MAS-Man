package kr.postech.monet.config.pool;

import kr.postech.monet.config.bean.SiteBean;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by woojoong on 2017-05-18.
 */
public class SiteConfPool {
    private List<SiteBean> siteBeans;

    public SiteConfPool() {
        siteBeans = new CopyOnWriteArrayList<SiteBean>();
    }

    public List<SiteBean> getSiteBeans() {
        return siteBeans;
    }

    public void setSiteBeans(List<SiteBean> siteBeans) {
        this.siteBeans = siteBeans;
    }
}
