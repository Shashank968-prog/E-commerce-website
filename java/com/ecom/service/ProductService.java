package com.ecom.service;

import java.util.List;
import com.ecom.model.Product;

public interface ProductService {
    Product saveProduct(Product product);
    List<Product> getAllProducts();
    Product getProductById(Integer id);
    boolean deleteProduct(Integer id);
}
