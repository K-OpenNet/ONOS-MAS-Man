package kr.postech.monet.core.monitor;

import kr.postech.monet.config.bean.SWBean;
import kr.postech.monet.config.bean.VMBean;
import kr.postech.monet.core.parser.RESTParser;
import kr.postech.monet.utils.RESTConnectionUtil;

import java.util.List;

/**
 * Created by woojoong on 2017-05-18.
 */
public class GettingCPTraffic {

    private final String CPTrafficRESTURL = "http://"
            + "<controllerIP>"
            + ":"
            + "<controllerPort>"
            + "/onos/cpman/controlmetrics/messages";

    public GettingCPTraffic() {
    }

    public String getCPTrafficRESTURL() {
        return CPTrafficRESTURL;
    }

    public List<SWBean> getCPTraffic(VMBean sourceVM, List<SWBean> sourceSWes) {

        String tmpRESTUrl = CPTrafficRESTURL.replace("<controllerIP>", sourceVM.getAccessIPAddress());
        tmpRESTUrl = tmpRESTUrl.replace("<controllerPort>", sourceVM.getAccessHTTPPort());

        RESTConnectionUtil restConn = new RESTConnectionUtil();
        String rawResults = restConn.getRESTToSingleVM(sourceVM, tmpRESTUrl);

        RESTParser restParser = new RESTParser();
        sourceSWes = restParser.parseGetCPTraffic(rawResults, sourceSWes);

        return sourceSWes;
    }

}
