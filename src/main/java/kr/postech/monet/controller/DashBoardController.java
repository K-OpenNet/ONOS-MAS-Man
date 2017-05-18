package kr.postech.monet.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by woojoong on 2017-05-18.
 */
public class DashBoardController {
    @RequestMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", "hello");
        return "dashboard";
    }
}
