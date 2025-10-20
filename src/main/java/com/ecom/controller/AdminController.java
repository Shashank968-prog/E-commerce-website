package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.service.CategoryService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    // ===================== Admin Dashboard =====================
    @GetMapping("/")
    public String adminHome() {
        return "admin/index";
    }

    // ===================== Load Add Product Page =====================
    @GetMapping("/loadAddProduct")
    public String loadAddProduct() {
        return "admin/add_product";
    }

    // ===================== Category Page =====================
    @GetMapping("/category")
    public String category(Model model) {
        List<Category> list = categoryService.getAllCategory();
        model.addAttribute("categories", list);
        return "admin/category";
    }

    // ===================== Save Category =====================
    @PostMapping("/saveCategory")
    public String saveCategory(@RequestParam("name") String name,
                               @RequestParam("isActive") Boolean isActive,
                               @RequestParam(value = "image", required = false) MultipartFile file,
                               HttpSession session) {

        try {
            Category category = new Category();
            category.setName(name);
            category.setIsActive(isActive);

            // Save uploaded image
            if (file != null && !file.isEmpty()) {
                String fileName = saveImageFile(file);
                if (fileName != null) {
                    category.setImageName(fileName);
                    System.out.println("Image saved successfully: " + fileName);
                } else {
                    session.setAttribute("errorMsg", "Failed to save image file!");
                    return "redirect:/admin/category";
                }
            }

            boolean isSaved = categoryService.saveCategory(category);

            if (isSaved) {
                session.setAttribute("successMsg", "Category saved successfully!");
            } else {
                session.setAttribute("errorMsg", "Error saving category!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Error: " + e.getMessage());
        }

        return "redirect:/admin/category";
    }

    // ===================== Delete Category =====================
    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session) {
        try {
            Category category = categoryService.getCategoryById(id);
            
            boolean isDeleted = categoryService.deleteCategory(id);

            if (isDeleted) {
                // Delete image file if exists
                if (category != null && category.getImageName() != null) {
                    deleteImageFile(category.getImageName());
                }
                session.setAttribute("successMsg", "Category deleted successfully!");
            } else {
                session.setAttribute("errorMsg", "Error deleting category!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Something went wrong during deletion!");
        }

        return "redirect:/admin/category";
    }

    // ===================== Edit Category =====================
    @GetMapping("/editCategory/{id}")
    public String editCategory(@PathVariable int id, Model model) {
        Category category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        return "admin/edit_category";
    }

    // ===================== Update Category =====================
    @PostMapping("/updateCategory")
    public String updateCategory(@RequestParam("id") int id,
                                 @RequestParam("name") String name,
                                 @RequestParam("isActive") Boolean isActive,
                                 @RequestParam(value = "image", required = false) MultipartFile file,
                                 HttpSession session) {

        try {
            Category category = categoryService.getCategoryById(id);
            category.setName(name);
            category.setIsActive(isActive);

            // Update image only if new one uploaded
            if (file != null && !file.isEmpty()) {
                // Delete old image if exists
                if (category.getImageName() != null) {
                    deleteImageFile(category.getImageName());
                }
                
                // Save new image
                String fileName = saveImageFile(file);
                if (fileName != null) {
                    category.setImageName(fileName);
                }
            }

            boolean isUpdated = categoryService.updateCategory(category);

            if (isUpdated) {
                session.setAttribute("successMsg", "Category updated successfully!");
            } else {
                session.setAttribute("errorMsg", "Error updating category!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Error: " + e.getMessage());
        }

        return "redirect:/admin/category";
    }

    // ===================== Helper Method: Save Image File =====================
    private String saveImageFile(MultipartFile file) {
        try {
            // Get the upload directory path
            File saveFile = new ClassPathResource("static/img").getFile();
            String uploadDir = saveFile.getAbsolutePath() + File.separator + "category_img";
            
            // Create directory if not exists
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
                System.out.println("Created directory: " + uploadDir);
            }

            // Generate unique filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            
            // Save file
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("File saved to: " + filePath.toString());
            return fileName;
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving file: " + e.getMessage());
            return null;
        }
    }

    // ===================== Helper Method: Delete Image File =====================
    private void deleteImageFile(String fileName) {
        try {
            File saveFile = new ClassPathResource("static/img").getFile();
            String uploadDir = saveFile.getAbsolutePath() + File.separator + "category_img";
            Path filePath = Paths.get(uploadDir, fileName);
            Files.deleteIfExists(filePath);
            System.out.println("Deleted file: " + filePath.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error deleting file: " + e.getMessage());
        }
    }
}