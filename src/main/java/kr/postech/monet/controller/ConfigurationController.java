package kr.postech.monet.controller;

import kr.postech.monet.config.GeneralConf;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by woojoong on 2017-05-18.
 */
@Controller
public class ConfigurationController {
    @RequestMapping("/configuration")
    public String configuration(Model model) {
        model.addAttribute("confList", GeneralConf.siteConfPoolList.getSiteBeans());
        return "configuration";
    }
}
