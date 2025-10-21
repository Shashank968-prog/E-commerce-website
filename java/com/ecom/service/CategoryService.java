package com.ecom.service;

import java.util.List;
import com.ecom.model.Category;

public interface CategoryService {
    boolean saveCategory(Category category);
    Boolean existCategory(String name);
    List<Category> getAllCategory();
    Category getCategoryById(Integer id);
    boolean deleteCategory(Integer id);  // or deleteCategoryById
    boolean updateCategory(Category category);
}