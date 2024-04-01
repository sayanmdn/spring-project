package com.sayantan.productservices.services;

import com.sayantan.productservices.dtos.FakeStoreProductDto;
import com.sayantan.productservices.models.Category;
import com.sayantan.productservices.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Primary
@Service
public abstract class FakeStoreProductService implements ProductService {

    private RestTemplate restTemplate;

    @Autowired
    public  FakeStoreProductService(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    private Product serialize(FakeStoreProductDto fakeStoreProduct){
        Product product = new Product();
        product.setId(fakeStoreProduct.getId());
        product.setTitle(fakeStoreProduct.getTitle());
        product.setPrice(fakeStoreProduct.getPrice());
        product.setDescription(fakeStoreProduct.getDescription());
        product.setImageUrl(fakeStoreProduct.getImage());

        product.setCategory(new Category());
        product.getCategory().setName(fakeStoreProduct.getCategory());

        return product;
    }
    @Override
    public Product getSingleProduct(Long id){
        System.out.println("In product service");
        FakeStoreProductDto productDto = restTemplate.getForObject("https://fakestoreapi.com/products/" + id, FakeStoreProductDto.class);
        return serialize(productDto);
    }

    @Override
    public List<Product> getAllProducts() {
        FakeStoreProductDto[] response = restTemplate.getForObject(
                "https://fakestoreapi.com/products",
                FakeStoreProductDto[].class
        );


        List<Product> answer = new ArrayList<>();


        for (FakeStoreProductDto dto: response) {
            answer.add(serialize(dto));
        }

        return answer;
    }

}
