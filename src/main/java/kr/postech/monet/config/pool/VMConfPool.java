package kr.postech.monet.config.pool;

import kr.postech.monet.config.bean.VMBean;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by woojoong on 2017-05-18.
 */
public class VMConfPool {

    private List<VMBean> vmBeans;

    public VMConfPool() {
        vmBeans = new CopyOnWriteArrayList<VMBean>();
    }

    public List<VMBean> getVmBeans() {
        return vmBeans;
    }

    public void setVmBeans(List<VMBean> vmBeans) {
        this.vmBeans = vmBeans;
    }
}
