package com.ecom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping; // ← Import needed

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/")
    public String index() {
        return "admin/index"; // This should map to src/main/resources/templates/admin/index.html
    }
    @GetMapping("/loadAddProduct")
    public String loadAddProduct() {
        return "admin/add_product"; // This should map to src/main/resources/templates/admin/index.html
    }
    
    @GetMapping("/category")
    public String category() {
        return "admin/category"; // This should map to src/main/resources/templates/admin/index.html
    }
}


