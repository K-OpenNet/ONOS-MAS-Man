package kr.postech.monet.core.database;

import kr.postech.monet.config.GeneralConf;
import kr.postech.monet.config.bean.PMBean;
import kr.postech.monet.config.bean.SiteBean;
import kr.postech.monet.config.bean.VMBean;
import kr.postech.monet.utils.SSHConnectionUtil;

/**
 * Created by woojoong on 2017-05-18.
 */
public class GetNumSwitches {
    private final String cmdQueryNumSwitches = "curl -GET http://localhost:8086/query --data-urlencode db=kict --data-urlencode \"q=<query>\"";
    private final String queryNumSwitches = "select time, value from numswitch" +
            " where \"site\" = '<site>' and \"pm\" = '<pm>' and \"vm\" = '<vm>' order by desc limit 10";

    public GetNumSwitches() {

    }

    public String getRawResultsQueryNumSwitches(SiteBean sourceSite, PMBean sourcePM, VMBean sourceVM) {
        String results = null;

        String tmpQuery = getQueryNumSwitches().replace("<site>", sourceSite.getSiteAlias());
        tmpQuery = tmpQuery.replace("<pm>", sourcePM.getPmAlias());
        tmpQuery = tmpQuery.replace("<vm>", sourceVM.getVmAlias());

        String tmpCommand = getCmdQueryNumSwitches().replace("<query>", tmpQuery);

        SSHConnectionUtil sshConn = new SSHConnectionUtil();
        results = sshConn.sendCmdToSingleVM(GeneralConf.dbBean, tmpCommand, 65535);

        return results;
    }

    public String getCmdQueryNumSwitches() {
        return cmdQueryNumSwitches;
    }

    public String getQueryNumSwitches() {
        return queryNumSwitches;
    }
}
