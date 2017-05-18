package kr.postech.monet.controller;

import kr.postech.monet.config.GeneralConf;
import kr.postech.monet.config.bean.PMBean;
import kr.postech.monet.config.bean.SiteBean;
import kr.postech.monet.config.bean.VMBean;
import kr.postech.monet.core.database.GetCPULoad;
import kr.postech.monet.core.database.GetControlTraffic;
import kr.postech.monet.core.database.GetNumSwitches;
import kr.postech.monet.core.database.bean.CPULoadBean;
import kr.postech.monet.core.database.bean.ControlTrafficBean;
import kr.postech.monet.core.database.bean.NumSwitchesBean;
import kr.postech.monet.core.parser.RESTParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;

/**
 * Created by woojoong on 2017-05-18.
 */
@Controller
public class MonitoringController {
    @RequestMapping("/monitoring")
    public String monitoring(Model model) {
        model.addAttribute("confList", GeneralConf.siteConfPoolList.getSiteBeans());

        GetControlTraffic ct = new GetControlTraffic();
        GetCPULoad cl = new GetCPULoad();
        GetNumSwitches nw = new GetNumSwitches();

        RESTParser parser = new RESTParser();

        HashMap<VMBean, List<ControlTrafficBean>> controlTrafficResults = new HashMap<VMBean, List<ControlTrafficBean>>();
        HashMap<VMBean, List<CPULoadBean>> cpuLoadResults = new HashMap<VMBean, List<CPULoadBean>>();
        HashMap<VMBean, List<NumSwitchesBean>> numSwitchesResults = new HashMap<VMBean, List<NumSwitchesBean>>();

        for (int index1 = 0; index1 < GeneralConf.siteConfPoolList.getSiteBeans().size(); index1++) {
            SiteBean tmpSite = GeneralConf.siteConfPoolList.getSiteBeans().get(index1);
            List<PMBean> pmBeanList = tmpSite.getPmConfPool().getPmBeans();
            for (int index2 = 0; index2 < pmBeanList.size(); index2++) {
                PMBean tmpPm = pmBeanList.get(index2);
                List<VMBean> vmBeanList = tmpPm.getVmConfPool().getVmBeans();
                for (int index3 = 0; index3 < vmBeanList.size(); index3++) {
                    VMBean tmpVm = vmBeanList.get(index3);
                    controlTrafficResults.put(tmpVm, parser.parseDBControlTraffic(ct.getRawResultsQueryControlTraffic(tmpSite, tmpPm, tmpVm)));
                    cpuLoadResults.put(tmpVm, parser.parseDBCPULoad(cl.getRawResultsQueryCPULoad(tmpSite, tmpPm, tmpVm)));
                    numSwitchesResults.put(tmpVm, parser.parseDBNumSwitches(nw.getRawResultsQueryNumSwitches(tmpSite, tmpPm, tmpVm)));
                }
            }
        }

        model.addAttribute("controlTrafficResults", controlTrafficResults);
        model.addAttribute("cpuLoadResults", cpuLoadResults);
        model.addAttribute("numSwitchesResults", numSwitchesResults);


        return "monitoring";
    }
}
