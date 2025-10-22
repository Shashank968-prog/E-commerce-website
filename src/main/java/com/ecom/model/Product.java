package com.ecom.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Product {  // Changed from 'product' to 'Product'
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(length = 500)
    private String title;
    
    @Column(length = 5000)
    private String description;  // Changed from 'Description' to 'description'
    
    @ManyToOne  // Changed from String to Category object
    private Category category;
    
    private Double price;
    
    private Integer stock;
    
    private String image;
    
    private Integer discount;  // Added
    
    private Double discountPrice;  // Added
    
    private Boolean isActive;  // Added
}