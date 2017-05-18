package kr.postech.monet.core.database;

import kr.postech.monet.config.GeneralConf;
import kr.postech.monet.config.bean.PMBean;
import kr.postech.monet.config.bean.SiteBean;
import kr.postech.monet.config.bean.VMBean;
import kr.postech.monet.utils.SSHConnectionUtil;

/**
 * Created by woojoong on 2017-05-18.
 */
public class GetControlTraffic {
    private final String cmdQueryControlTraffic = "curl -GET http://localhost:8086/query --data-urlencode db=kict --data-urlencode \"q=<query>\"";
    private final String queryControlTraffic = "select time, inbound, outbound, flowmod, flowrem, statreq, statrep, value from controltraffic" +
            " where \"site\" = '<site>' and \"pm\" = '<pm>' and \"vm\" = '<vm>' order by desc limit 10";

    public GetControlTraffic() {

    }

    public String getRawResultsQueryControlTraffic(SiteBean sourceSite, PMBean sourcePM, VMBean sourceVM) {
        String results = null;

        String tmpQuery = getQueryControlTraffic().replace("<site>", sourceSite.getSiteAlias());
        tmpQuery = tmpQuery.replace("<pm>", sourcePM.getPmAlias());
        tmpQuery = tmpQuery.replace("<vm>", sourceVM.getVmAlias());

        String tmpCommand = getCmdQueryControlTraffic().replace("<query>", tmpQuery);

        SSHConnectionUtil sshConn = new SSHConnectionUtil();
        results = sshConn.sendCmdToSingleVM(GeneralConf.dbBean, tmpCommand, 65535);

        return results;
    }

    public String getCmdQueryControlTraffic() {
        return cmdQueryControlTraffic;
    }

    public String getQueryControlTraffic() {
        return queryControlTraffic;
    }
}
