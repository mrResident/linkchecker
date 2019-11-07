package ru.resprojects.linkchecker.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EntryPointController {

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

}
