package com.sayantan.productservices.controllers;

import com.sayantan.productservices.models.Product;
import com.sayantan.productservices.services.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class ProductControllerTest {
    @Autowired
    private ProductController productController;

    @MockBean
    private ProductService productService;

//    @MockBean
//    private ProductRepository productRepository;

    @Test
    void testProductsSameAsService() {
        // arrange
        List<Product> products = new ArrayList<>();
        Product p1 = new Product(); // o
        p1.setTitle("iPhone 15");
        products.add(p1);

        Product p2 = new Product(); // p
        p2.setTitle("iPhone 15 Pro");
        products.add(p2);

        Product p3 = new Product(); // q
        p3.setTitle("iPhone 15 Pro Max");
        products.add(p3);

        List<Product> prodctsToPass = new ArrayList<>();

        for (Product p : products) {
            Product p111 = new Product();
            p111.setTitle(p.getTitle());
            prodctsToPass.add(p111);
        }

        when(
                productService.getAllProducts(pageable)
        ).thenReturn(
                prodctsToPass
        );


        // act
        ResponseEntity<List<Product>> response =
                productController.getAllProducts();

        // assert
        List<Product> productsInResponse = response.getBody();

        assertEquals(products.size(), productsInResponse.size());

        for (int i = 0; i < productsInResponse.size(); ++i)
            assertEquals(
                    products.get(i).getTitle(), // o p q
                    productsInResponse.get(i).getTitle()
            );
    }

}