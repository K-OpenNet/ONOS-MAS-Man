package kr.postech.monet.config.pool;

import kr.postech.monet.config.bean.PMBean;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by woojoong on 2017-05-18.
 */
public class PMConfPool {
    private List<PMBean> pmBeans;

    public PMConfPool() {
        pmBeans = new CopyOnWriteArrayList<PMBean>();
    }

    public List<PMBean> getPmBeans() {
        return pmBeans;
    }

    public void setPmBeans(List<PMBean> pmBeans) {
        this.pmBeans = pmBeans;
    }
}
