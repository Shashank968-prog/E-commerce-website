package com.ecom.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;  // Add this if you create ProductService
import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.repositories.ProductRepository;

@Controller
public class HomeController {
    @Autowired
    private CategoryService categoryservice;

    @Autowired
    private ProductService productService;  // This will work after creating ProductService

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/products")
    public String products(Model model) {
        List<Category> categories = categoryservice.getAllCategory(); // Fixed
        List<Product> products = productService.getAllProducts();
        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        return "products";
    }

    @GetMapping("/product")
    public String viewProduct() {
        return "veiw_product";
    }
}