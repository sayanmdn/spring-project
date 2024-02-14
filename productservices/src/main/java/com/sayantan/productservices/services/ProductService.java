package com.sayantan.productservices.services;

import com.sayantan.productservices.models.Product;
import org.springframework.stereotype.Service;

@Service
public interface ProductService {
    Product getSingleProduct(Long id);

}
