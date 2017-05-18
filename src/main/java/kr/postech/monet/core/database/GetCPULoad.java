package kr.postech.monet.core.database;

import kr.postech.monet.config.GeneralConf;
import kr.postech.monet.config.bean.PMBean;
import kr.postech.monet.config.bean.SiteBean;
import kr.postech.monet.config.bean.VMBean;
import kr.postech.monet.utils.SSHConnectionUtil;

/**
 * Created by woojoong on 2017-05-18.
 */
public class GetCPULoad {
    private final String cmdQueryCPULoad = "curl -GET http://localhost:8086/query --data-urlencode db=kict --data-urlencode \"q=<query>\"";
    private final String queryCPULoad = "select time, value from cpuload" +
            " where \"site\" = '<site>' and \"pm\" = '<pm>' and \"vm\" = '<vm>' order by desc limit 10";

    public GetCPULoad() {
    }

    public String getRawResultsQueryCPULoad(SiteBean sourceSite, PMBean sourcePM, VMBean sourceVM) {
        String results = null;

        String tmpQuery = getQueryCPULoad().replace("<site>", sourceSite.getSiteAlias());
        tmpQuery = tmpQuery.replace("<pm>", sourcePM.getPmAlias());
        tmpQuery = tmpQuery.replace("<vm>", sourceVM.getVmAlias());

        String tmpCommand = getCmdQueryCPULoad().replace("<query>", tmpQuery);

        SSHConnectionUtil sshConn = new SSHConnectionUtil();
        results = sshConn.sendCmdToSingleVM(GeneralConf.dbBean, tmpCommand, 65535);

        return results;
    }

    public String getCmdQueryCPULoad() {
        return cmdQueryCPULoad;
    }

    public String getQueryCPULoad() {
        return queryCPULoad;
    }
}
