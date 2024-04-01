package com.sayantan.productservices.services;

import com.sayantan.productservices.exceptions.ProductNotExistsException;
import com.sayantan.productservices.models.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductService {
    Product getSingleProduct(Long id) throws ProductNotExistsException;

    List<Product> getAllProducts();

    Product updateProduct(Long id, Product product);

    Product replaceProduct(Long id, Product product);

    Product addNewProduct(Product product);

    boolean deleteProduct(Long id);
}
