package com.ecom.controller;

import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.ecom.model.Category;
import com.ecom.service.CategoryService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private CategoryService categoryService;
    
    @GetMapping("/")
    public String index() {
        return "admin/index";
    }
    
    @GetMapping("/loadAddProduct")
    public String loadAddProduct() {
        return "admin/add_product";
    }
    
    @GetMapping("/category")
    public String category(Model model, HttpSession session) {
        List<Category> categories = categoryService.getAllCategory();
        model.addAttribute("categories", categories);
        
        // Clear session messages after displaying
        session.removeAttribute("successMsg");
        session.removeAttribute("errorMsg");
        
        return "admin/category";
    }
    
    @PostMapping("/saveCategory")
    public String saveCategory(@ModelAttribute Category category,
                               @RequestParam("image") MultipartFile file,
                               HttpSession session) throws IOException {
        
        // Set image name and data
        if (!file.isEmpty()) {
            category.setImageName(file.getOriginalFilename());
            category.setImageData(file.getBytes());
        } else {
            category.setImageName("default.jpg");
            category.setImageData(null);
        }
        
        // DEBUG: Print what we're trying to save
        System.out.println("========== DEBUG INFO ==========");
        System.out.println("Trying to save category: [" + category.getName() + "]");
        System.out.println("Image name: " + category.getImageName());
        System.out.println("Image size: " + (category.getImageData() != null ? category.getImageData().length + " bytes" : "null"));
        System.out.println("Is Active: " + category.getIsActive());
        
        // Check existing categories
        List<Category> allCategories = categoryService.getAllCategory();
        System.out.println("Total categories in DB: " + allCategories.size());
        for(Category c : allCategories) {
            System.out.println("  - ID: " + c.getId() + ", Name: [" + c.getName() + "]");
        }
        
        boolean exists = categoryService.existCategory(category.getName());
        System.out.println("existsByName result: " + exists);
        System.out.println("================================");
        
        if (exists) {
            session.setAttribute("errorMsg", "Category name already exists");
        } else {
            boolean isSaved = categoryService.saveCategory(category);
            
            if (isSaved) {
                session.setAttribute("successMsg", "Category saved successfully");
                System.out.println("Category saved successfully in database!");
            } else {
                session.setAttribute("errorMsg", "Category not saved! Internal server error");
            }
        }
        
        return "redirect:/admin/category";
    }
    
    // Endpoint to retrieve image from database
    @GetMapping("/category/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getCategoryImage(@PathVariable Integer id) {
        Category category = categoryService.getCategoryById(id);
        
        if (category != null && category.getImageData() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(category.getImageData());
        }
        
        return ResponseEntity.notFound().build();
    }
}