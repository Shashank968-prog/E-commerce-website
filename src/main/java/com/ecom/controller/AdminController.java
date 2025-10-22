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
import org.springframework.data.domain.Page;
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
import com.ecom.model.Product;
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
    @GetMapping("/")
    public String adminHome() {
        return "admin/index";
    }

    // ===================== View All Products with Pagination =====================
    @GetMapping("/products")
    public String viewProducts(
            @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "ch", required = false) String ch,
            Model model) {

        Page<Product> page = null;

        if (ch != null && !ch.isEmpty()) {
            // Search products by title or category
            page = productService.searchProductPagination(pageNo, pageSize, ch);
        } else {
            // Get all products with pagination
            page = productService.getAllProductsPagination(pageNo, pageSize);
        }

        model.addAttribute("products", page.getContent());
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/products";
    }

    // ===================== Load Add Product Page =====================
    @GetMapping("/loadAddProduct")
    public String loadAddProduct(Model model) { 
        List<Category> categories = categoryService.getAllCategory();
        model.addAttribute("categories", categories); 
        model.addAttribute("product", new Product());
        return "admin/add_product";
    }
    
    // ===================== Save Product =====================
    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product, 
                              @RequestParam("file") MultipartFile image,
                              HttpSession session) {

        try {
            String uniqueFileName;
            if (image.isEmpty()) {
                uniqueFileName = "default.jpg";
            } else {
                String originalFileName = StringUtils.cleanPath(image.getOriginalFilename());
                uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;
            }

            product.setImage(uniqueFileName);
            product.setDiscount(0);
            product.setDiscountPrice(product.getPrice());
            
            Product savedProduct = productService.saveProduct(product);

            if (!ObjectUtils.isEmpty(savedProduct) && !image.isEmpty()) {
                File uploadDir = new ClassPathResource("static/img").getFile();
                String productUploadPath = uploadDir.getAbsolutePath() + File.separator + "product_img";

                File directory = new File(productUploadPath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                Path targetPath = Paths.get(productUploadPath + File.separator + uniqueFileName);
                Files.copy(image.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                session.setAttribute("succMsg", "Product Saved Successfully! 🎉");
            } else if (ObjectUtils.isEmpty(savedProduct)) {
                session.setAttribute("errorMsg", "Something went wrong on the server during database save.");
            } else {
                session.setAttribute("succMsg", "Product Saved Successfully (using default image).");
            }

        } catch (IOException e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "File upload failed: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "An unexpected error occurred: " + e.getMessage());
        }

        return "redirect:/admin/loadAddProduct";
    }

    // ===================== Edit Product Page =====================
    @GetMapping("/editProduct/{id}")
    public String editProduct(@PathVariable Integer id, Model model) {
        Product product = productService.getProductById(id);
        List<Category> categories = categoryService.getAllCategory();
        
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        
        return "admin/edit_product";
    }

    // ===================== Update Product =====================
    @PostMapping("/updateProduct")
    public String updateProduct(@ModelAttribute Product product, 
                                @RequestParam("file") MultipartFile image,
                                HttpSession session) {

        try {
            Product updatedProduct = productService.updateProduct(product, image);

            if (!ObjectUtils.isEmpty(updatedProduct)) {
                session.setAttribute("succMsg", "Product Updated Successfully! ✅");
            } else {
                session.setAttribute("errorMsg", "Error updating product!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Error: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }

    // ===================== Delete Product =====================
    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable Integer id, HttpSession session) {
        try {
            Product product = productService.getProductById(id);
            
            Boolean isDeleted = productService.deleteProduct(id);

            if (isDeleted) {
                if (product != null && product.getImage() != null && !product.getImage().equals("default.jpg")) {
                    deleteImageFile(product.getImage(), "product_img");
                }
                session.setAttribute("succMsg", "Product deleted successfully! 🗑️");
            } else {
                session.setAttribute("errorMsg", "Error deleting product!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Something went wrong during deletion!");
        }

        return "redirect:/admin/products";
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
    public String saveCategory(
            @RequestParam("name") String name,
            @RequestParam("isActive") Boolean isActive,
            @RequestParam(value = "image", required = false) MultipartFile file,
            HttpSession session) {

        try {
            Category category = new Category();
            category.setName(name);
            category.setIsActive(isActive);

            if (file != null && !file.isEmpty()) {
                String fileName = saveImageFile(file, "category_img");
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

    // ===================== Delete Category =====================
    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session) {
        try {
            Category category = categoryService.getCategoryById(id);
            
            boolean isDeleted = categoryService.deleteCategory(id);

            if (isDeleted) {
                if (category != null && category.getImageName() != null) {
                    deleteImageFile(category.getImageName(), "category_img");
                }
                session.setAttribute("successMsg", "Category deleted successfully! 🗑️");
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

            if (file != null && !file.isEmpty()) {
                if (oldImageName != null) {
                    deleteImageFile(oldImageName, "category_img");
                }
                
                String fileName = saveImageFile(file, "category_img");
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

    // ===================== Helper Methods =====================
    private String saveImageFile(MultipartFile file, String subDirectory) {
        try {
            File saveFile = new ClassPathResource("static/img").getFile();
            String uploadDir = saveFile.getAbsolutePath() + File.separator + subDirectory;
            
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return fileName;
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void deleteImageFile(String fileName, String subDirectory) {
        try {
            File saveFile = new ClassPathResource("static/img").getFile();
            String uploadDir = saveFile.getAbsolutePath() + File.separator + subDirectory;
            Path filePath = Paths.get(uploadDir, fileName);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}