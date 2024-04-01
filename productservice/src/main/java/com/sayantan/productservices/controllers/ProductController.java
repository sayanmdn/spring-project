package com.sayantan.productservices.controllers;

import com.sayantan.productservices.commons.AuthenticationCommons;
import com.sayantan.productservices.models.Product;
import com.sayantan.productservices.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    private RestTemplate restTemplate;

    private AuthenticationCommons authenticationCommons;

    @Autowired
    public ProductController(@Qualifier("selfProductService") ProductService productService,
                             RestTemplate restTemplate,
                             AuthenticationCommons authorizationCommons) {
        this.productService = productService;
        this.restTemplate = restTemplate;
        this.authenticationCommons = authorizationCommons;
    }


    // Constructor injection
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public ResponseEntity<List<Product>> getAllProducts(){
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product>  getProduct(@PathVariable("id") Long id){
        System.out.println("In product controller");

        try{
            return new ResponseEntity<>(
                    productService.getSingleProduct(id),
                    HttpStatus.OK
            );
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping("/add")
    public Product addNewProduct(@RequestBody Product product){
        Product p = product;
        p.setTitle("Default Title");
        return p;
    }

    @PatchMapping("/{id}")
    public Product updateProduct(@PathVariable("id") Long id, @RequestBody Product product){
    return new Product();
    }

    @PutMapping("/{id}")
    public Product replaceProduct(@PathVariable("id") Long id, @RequestBody Product product) {
        return new Product();
    }

    // Delete a product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
