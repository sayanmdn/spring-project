package com.sayantan.productservices.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class ProductSearchCriteria {
    private String name;
    private String category;
    private String brand;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean available;
    private Double minRating;
    private Map<String, String> customAttributes;
}
