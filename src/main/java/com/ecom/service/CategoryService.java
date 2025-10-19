package com.ecom.service;

import java.util.List;
import com.ecom.model.Category;

public interface CategoryService {
    boolean saveCategory(Category category);
    Boolean existCategory(String name);
    List<Category> getAllCategory();
    Category getCategoryById(Integer id);
    // Add this line
    boolean deleteCategory(Integer id);
    boolean updateCategory(Category category);
}