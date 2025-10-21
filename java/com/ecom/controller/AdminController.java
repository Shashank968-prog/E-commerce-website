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
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product; // FIX: Ensure this path is correct and the file exists
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private ProductService productService;

    // ===================== Admin Dashboard =====================
    /**
     * Loads the admin dashboard home page.
     */
    @GetMapping("/")
    public String adminHome() {
        return "admin/index";
    }

    // -----------------------------------------------------------

    // ===================== Load Add Product Page =====================
    /**
     * Loads the add product page and adds the list of all categories to the model.
     * @param model Spring Model for view data
     */
    @GetMapping("/loadAddProduct")
    public String loadAddProduct(Model model) { 
        List<Category> categories = categoryService.getAllCategory();
        model.addAttribute("categories", categories); 
        model.addAttribute("product", new Product()); // Add empty product for form binding
        return "admin/add_product";
    }
    
    // -----------------------------------------------------------

    // ===================== Save Product =====================
    /**
     * Handles the form submission to save a new product, including image upload.
     */
    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product, 
                              @RequestParam("file") MultipartFile image,
                              HttpSession session) {

        try {
            // 1. Generate a unique file name
            String uniqueFileName;
            if (image.isEmpty()) {
                uniqueFileName = "default.jpg";
            } else {
                // Use current time + original name for a high chance of uniqueness
                String originalFileName = StringUtils.cleanPath(image.getOriginalFilename());
                uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;
            }

            // 2. Set product details BEFORE saving
            product.setImage(uniqueFileName); // Store the unique name in the database
            product.setDiscount(0);
            product.setDiscountPrice(product.getPrice());
            
            // 3. Save the product entity to get the persisted object
            Product savedProduct = productService.saveProduct(product);

            // 4. Handle file system saving only if the database save was successful
            if (!ObjectUtils.isEmpty(savedProduct) && !image.isEmpty()) {

                // Define the target directory path
                File uploadDir = new ClassPathResource("static/img").getFile();
                String productUploadPath = uploadDir.getAbsolutePath() + File.separator + "product_img";

                // Ensure the product image directory exists
                File directory = new File(productUploadPath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Create the final file path using the unique file name
                Path targetPath = Paths.get(productUploadPath + File.separator + uniqueFileName);

                // Copy the file
                Files.copy(image.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                session.setAttribute("succMsg", "Product Saved Successfully! 🎉");
            } else if (ObjectUtils.isEmpty(savedProduct)) {
                // Handle database save failure
                session.setAttribute("errorMsg", "Something went wrong on the server during database save.");
            } else {
                // Case: Product saved, but no image uploaded (using default.jpg)
                session.setAttribute("succMsg", "Product Saved Successfully (using default image).");
            }

        } catch (IOException e) {
            // Catch file system errors
            e.printStackTrace();
            session.setAttribute("errorMsg", "File upload failed: " + e.getMessage());
        } catch (Exception e) {
            // Catch service layer/general errors
            e.printStackTrace();
            session.setAttribute("errorMsg", "An unexpected error occurred: " + e.getMessage());
        }

        return "redirect:/admin/loadAddProduct";
    }
    // -----------------------------------------------------------

    // ===================== Category Page =====================
    /**
     * Loads the category listing page with all existing categories.
     * @param model Spring Model for view data
     */
    @GetMapping("/category")
    public String category(Model model) {
        List<Category> list = categoryService.getAllCategory();
        model.addAttribute("categories", list);
        return "admin/category";
    }

    // -----------------------------------------------------------

    // ===================== Save Category =====================
    /**
     * Handles the form submission to save a new category.
     */
    @PostMapping("/saveCategory")
    public String saveCategory(
            @RequestParam("name") String name,
            @RequestParam("isActive") Boolean isActive,
            @RequestParam(value = "image", required = false) MultipartFile file,
            HttpSession session) {

        try {
            Category category = new Category();
            category.setName(name);
            category.setIsActive(isActive);

            // Save uploaded image
            if (file != null && !file.isEmpty()) {
                String fileName = saveImageFile(file, "category_img"); // Using reusable helper
                if (fileName != null) {
                    category.setImageName(fileName);
                } else {
                    session.setAttribute("errorMsg", "Failed to save image file!");
                    return "redirect:/admin/category";
                }
            }

            boolean isSaved = categoryService.saveCategory(category);

            if (isSaved) {
                session.setAttribute("successMsg", "Category saved successfully! 🎉");
            } else {
                session.setAttribute("errorMsg", "Error saving category!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Error: " + e.getMessage());
        }

        return "redirect:/admin/category";
    }

    // -----------------------------------------------------------

    // ===================== Delete Category =====================
    /**
     * Deletes a category by its ID, including the associated image file.
     * @param id The ID of the category to delete
     */
    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session) {
        try {
            Category category = categoryService.getCategoryById(id);
            
            boolean isDeleted = categoryService.deleteCategory(id);

            if (isDeleted) {
                // Delete image file if exists
                if (category != null && category.getImageName() != null) {
                    deleteImageFile(category.getImageName(), "category_img"); // Using reusable helper
                }
                session.setAttribute("successMsg", "Category deleted successfully!🗑️");
            } else {
                session.setAttribute("errorMsg", "Error deleting category!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Something went wrong during deletion!");
        }

        return "redirect:/admin/category";
    }

    // -----------------------------------------------------------

    // ===================== Edit Category =====================
    /**
     * Loads the category edit page for a specific category.
     * @param id The ID of the category to edit
     * @param model Spring Model for view data
     */
    @GetMapping("/editCategory/{id}")
    public String editCategory(@PathVariable int id, Model model) {
        Category category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        return "admin/edit_category";
    }

    // -----------------------------------------------------------

    // ===================== Update Category =====================
    /**
     * Handles the form submission to update an existing category.
     */
    @PostMapping("/updateCategory")
    public String updateCategory(
            @RequestParam("id") int id,
            @RequestParam("name") String name,
            @RequestParam("isActive") Boolean isActive,
            @RequestParam(value = "image", required = false) MultipartFile file,
            HttpSession session) {

        try {
            Category category = categoryService.getCategoryById(id);
            String oldImageName = category.getImageName();
            category.setName(name);
            category.setIsActive(isActive);

            // Update image only if a new one is uploaded
            if (file != null && !file.isEmpty()) {
                // Delete old image if it exists
                if (oldImageName != null) {
                    deleteImageFile(oldImageName, "category_img"); // Using reusable helper
                }
                
                // Save new image
                String fileName = saveImageFile(file, "category_img"); // Using reusable helper
                if (fileName != null) {
                    category.setImageName(fileName);
                }
            }

            boolean isUpdated = categoryService.updateCategory(category);

            if (isUpdated) {
                session.setAttribute("successMsg", "Category updated successfully! ✅");
            } else {
                session.setAttribute("errorMsg", "Error updating category!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Error: " + e.getMessage());
        }

        return "redirect:/admin/category";
    }

    // -----------------------------------------------------------
    
    // ===================== Helper Method: Save Product Image File (Specific) =====================
    /**
     * Convenience method specifically for saving product images to the 'product_img' folder.
     */
    private String saveProductImageFile(MultipartFile file) {
        return saveImageFile(file, "product_img");
    }

    // ===================== Helper Method: Save Image File (Generic) =====================
    /**
     * Saves the uploaded MultipartFile to the specified subdirectory within static/img.
     * @param file The MultipartFile object.
     * @param subDirectory The folder name (e.g., "category_img" or "product_img").
     * @return The unique filename or null on failure.
     */
    private String saveImageFile(MultipartFile file, String subDirectory) {
        try {
            File saveFile = new ClassPathResource("static/img").getFile();
            String uploadDir = saveFile.getAbsolutePath() + File.separator + subDirectory;
            
            // Create directory if not exists
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate unique filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            
            // Save file
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return fileName;
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ===================== Helper Method: Delete Image File (Generic) =====================
    /**
     * Deletes an image file from the specified subdirectory within the filesystem.
     * @param fileName The name of the file to delete.
     * @param subDirectory The folder name (e.g., "category_img" or "product_img").
     */
    private void deleteImageFile(String fileName, String subDirectory) {
        try {
            File saveFile = new ClassPathResource("static/img").getFile();
            String uploadDir = saveFile.getAbsolutePath() + File.separator + subDirectory;
            Path filePath = Paths.get(uploadDir, fileName);
            
            // Check if file exists before attempting to delete
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}