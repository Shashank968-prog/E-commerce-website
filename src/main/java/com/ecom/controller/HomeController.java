package com.ecom.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    // ===================== Home Page =====================
    @GetMapping("/")
    public String index(Model model) {
        // Get active categories for homepage
        List<Category> categories = categoryService.getAllActiveCategory();
        List<Product> products = productService.getAllActiveProducts();
        
        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        
        return "index";
    }

    // ===================== Login Page =====================
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ===================== Register Page =====================
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    // ===================== All Products Page with Search =====================
    @GetMapping("/products")
    public String products(Model model, @RequestParam(name = "ch", required = false) String search) {
        
        // ✅ Get only ACTIVE categories (this fixes the numbers issue!)
        List<Category> categories = categoryService.getAllActiveCategory();
        
        List<Product> products;
        
        // If search query exists, search products
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProduct(search.trim());
        } else {
            // Get all active products
            products = productService.getAllActiveProducts();
        }
        
        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        model.addAttribute("searchQuery", search);
        
        return "products";
    }

    // ===================== Products by Category =====================
    @GetMapping("/products/category/{id}")
    public String productsByCategory(@PathVariable Integer id, Model model) {
        
        // Get all active categories for sidebar
        List<Category> categories = categoryService.getAllActiveCategory();
        
        // Get selected category
        Category selectedCategory = categoryService.getCategoryById(id);
        
        List<Product> products;
        
        if (!ObjectUtils.isEmpty(selectedCategory)) {
            // Get products by category name (your existing method uses String)
            products = productService.getAllActiveProducts(selectedCategory.getName());
            model.addAttribute("selectedCategory", selectedCategory);
        } else {
            // If category not found, show all products
            products = productService.getAllActiveProducts();
        }
        
        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        
        return "products";
    }

    // ===================== View Single Product =====================
    @GetMapping("/product/{id}")
    public String viewProduct(@PathVariable Integer id, Model model) {
        
        Product product = productService.getProductById(id);
        
        if (ObjectUtils.isEmpty(product)) {
            // If product not found, redirect to products page
            return "redirect:/products";
        }
        
        model.addAttribute("product", product);
        
        return "view_product";
    }
}