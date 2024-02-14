package com.sayantan.productservices.services;

import com.sayantan.productservices.dtos.FakeStoreProductDto;
import com.sayantan.productservices.models.Category;
import com.sayantan.productservices.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FakeStoreProductService implements ProductService {

    private RestTemplate restTemplate;

    @Autowired
    public  FakeStoreProductService(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    private Product serialize(FakeStoreProductDto fakeStoreProduct){
        Product product = new Product();
        product.setId(product.getId());
        product.setTitle(fakeStoreProduct.getTitle());
        product.setCategory(new Category());
        product.setPrice(fakeStoreProduct.getPrice());
        product.setDescription(fakeStoreProduct.getDescription());
        product.setImageUrl(fakeStoreProduct.getImage());
        product.getCategory().setName(fakeStoreProduct.getCategory());

        return product;
    }
    @Override
    public Product getSingleProduct(Long id){
        System.out.println("In product service");
        FakeStoreProductDto productDto = restTemplate.getForObject("https://fakestoreapi.com/products/" + id, FakeStoreProductDto.class);
        return serialize(productDto);
    }

}
