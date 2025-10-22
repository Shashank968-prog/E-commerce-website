package com.ecom.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.ecom.model.Category;
import com.ecom.repositories.CategoryRepository;
import com.ecom.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Override
    public boolean saveCategory(Category category) {
        Category savedCategory = categoryRepository.save(category);
        return !ObjectUtils.isEmpty(savedCategory);
    }
    
    @Override
    public List<Category> getAllCategory() {
        return categoryRepository.findAll();
    }
    
    @Override
    public Boolean existCategory(String name) {
        return categoryRepository.existsByName(name);
    }
    
    @Override
    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }
    
    @Override
    public boolean deleteCategory(Integer id) {
        try {
            Category category = categoryRepository.findById(id).orElse(null);
            if (category != null) {
                categoryRepository.delete(category);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateCategory(Category category) {
        try {
            Category existingCategory = categoryRepository.findById(category.getId()).orElse(null);
            if (existingCategory != null) {
                Category updatedCategory = categoryRepository.save(category);
                return !ObjectUtils.isEmpty(updatedCategory);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}