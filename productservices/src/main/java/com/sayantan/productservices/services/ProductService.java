package com.sayantan.productservices.services;

import com.sayantan.productservices.models.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductService {
    Product getSingleProduct(Long id);

    List<Product> getAllProducts();

}
