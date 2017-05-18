package kr.postech.monet.core.parser;

import kr.postech.monet.config.bean.PMBean;
import kr.postech.monet.config.bean.VMBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

/**
 * Created by woojoong on 2017-05-18.
 */
public class SSHParser {
    public HashMap<VMBean, Float> parseCPULoad(String rawResults, PMBean sourcePM) {
        HashMap<VMBean, Float> resultHashMap = new HashMap<VMBean, Float>();

        if (rawResults == null) {
            return resultHashMap;
        }

        StringReader sr = new StringReader(rawResults);
        BufferedReader br = new BufferedReader(sr);

        try {
            br.readLine(); // header
            br.readLine(); // divider
            br.readLine(); // host - CPU/User
            br.readLine(); // host - CPU/Kernel

            String temp = null;
            List<VMBean> vmBeanList = sourcePM.getVmConfPool().getVmBeans();
            while ((temp = br.readLine()) != null) {
                for (int index = 0; index < vmBeanList.size(); index++) {
                    if(vmBeanList.get(index).getVmAlias().equals(temp.split("\\s+")[0])) {

                        float loadCPUUser = Float.valueOf(temp.split("\\s+")[2].substring(0, temp.split("\\s+")[2].length()-1));
                        temp = br.readLine();
                        float loadCPUKernel = Float.valueOf(temp.split("\\s+")[2].substring(0, temp.split("\\s+")[2].length()-1));
                        resultHashMap.put(vmBeanList.get(index), loadCPUKernel+loadCPUUser);
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultHashMap;
    }
}
