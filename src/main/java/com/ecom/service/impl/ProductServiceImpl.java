package com.ecom.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.Product;
import com.ecom.repositories.ProductRepository;
import com.ecom.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {
	@Autowired
	private ProductRepository productRepository;

	@Override
	public Product saveProduct(Product product) {
		// TODO Auto-generated method stub
		return productRepository.save(product);
	}

	@Override
	public List<Product> getAllProducts() {
		// TODO Auto-generated method stub
		return productRepository.findAll();
	}

	@Override
	public Product getProductById(Integer id) {
		// TODO Auto-generated method stub
		Optional<Product> product = productRepository.findById(id);
		return product.orElse(null);
	}

	@Override
	public boolean deleteProduct(Integer id) {
		// TODO Auto-generated method stub
		try {
			Optional<Product> product = productRepository.findById(id);
			if (product.isPresent()) {
				productRepository.deleteById(id);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}