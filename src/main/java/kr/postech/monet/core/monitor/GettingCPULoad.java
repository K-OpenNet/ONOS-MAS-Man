package kr.postech.monet.core.monitor;

import kr.postech.monet.config.bean.PMBean;
import kr.postech.monet.config.bean.VMBean;
import kr.postech.monet.core.parser.SSHParser;
import kr.postech.monet.utils.SSHConnectionUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

/**
 * Created by woojoong on 2017-05-18.
 */
public class GettingCPULoad {
    //private final String SSHCommandCPULoad = "vboxmanage metrics query '*' CPU/Load/User:avg,CPU/Load/Kernel:avg";
    private final String SSHCommandCPULoad = "top -n 1 -b | head -1 | awk '{print $12}'";
    public GettingCPULoad() {
    }

    public String getSSHCommandCPULoad() {
        return SSHCommandCPULoad;
    }

    public HashMap<VMBean, Float> getCPULoad(PMBean sourcePM) {
        HashMap<VMBean, Float> resultmap = new HashMap<VMBean, Float>();

        SSHConnectionUtil sshConn = new SSHConnectionUtil();
        SSHParser sshParser = new SSHParser();
        if(sourcePM.isAlive()) {

            List<VMBean> vmBeanList = sourcePM.getVmConfPool().getVmBeans();
            for (int index1 = 0; index1 < vmBeanList.size(); index1++) {
                VMBean tmpVm = vmBeanList.get(index1);
                if(tmpVm.isAlive()) {
                    String tmpResult = sshConn.sendCmdToSingleVM(tmpVm, SSHCommandCPULoad, 4096);
                    StringReader sr = new StringReader(tmpResult);
                    BufferedReader br = new BufferedReader(sr);
                    try {
                        tmpResult = br.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    tmpResult = tmpResult.replace("\n", "").replace(",", "");
                    resultmap.put(tmpVm, Float.valueOf(tmpResult));
                }
            }

            //String tmpResults = sshConn.sendCmdToSinglePM(sourcePM, SSHCommandCPULoad, 4096);
            //resultmap = sshParser.parseCPULoad(tmpResults, sourcePM);
        }

        return resultmap;
    }
}
