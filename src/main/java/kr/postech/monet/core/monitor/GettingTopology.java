package kr.postech.monet.core.monitor;

import kr.postech.monet.config.bean.SWBean;
import kr.postech.monet.config.bean.VMBean;
import kr.postech.monet.core.parser.RESTParser;
import kr.postech.monet.utils.RESTConnectionUtil;

import java.util.List;

/**
 * Created by woojoong on 2017-05-18.
 */
public class GettingTopology {
    public GettingTopology() {
    }

    public List<SWBean> getSwitches(VMBean sourceVM) {

        RESTConnectionUtil restConn = new RESTConnectionUtil();
        String cmdURL = "http://" + sourceVM.getAccessIPAddress() + ":" +
                sourceVM.getAccessHTTPPort() + "/onos/v1/devices";
        String restraw = restConn.getRESTToSingleVM(sourceVM, cmdURL);
        RESTParser parser = new RESTParser();
        return parser.parseGetSwitches(restraw);
    }

    public List<String> getMasterRoleSwitches(VMBean sourceVM) {

        RESTConnectionUtil restConn = new RESTConnectionUtil();
        String cmdURL = "http://" + sourceVM.getAccessIPAddress() + ":" + sourceVM.getAccessHTTPPort()
                + "/onos/v1/mastership/" + sourceVM.getIpAddress() + "/device";
        String restraw = restConn.getRESTToSingleVM(sourceVM, cmdURL);
        RESTParser parser = new RESTParser();

        return parser.parseGetMasterRoleSwitches(restraw);
    }
}
