package com.ecom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @GetMapping("/login")
    public String login() {  // Changed method name
        return "login";
    }
    
    @GetMapping("/register")
    public String register() {  // Changed method name
        return "register";
    }
}
