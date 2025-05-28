package com.sayantan.productservices.dtos;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.*;
        import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ProductCreateDto {
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private Long subcategoryId;

    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    private String brand;

    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    private BigDecimal weight;

    private String dimensions;
    private Map<String, Object> customAttributes;
    private List<String> tags;
}

@Data
@Builder
public class ProductUpdateDto {
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    private String brand;

    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    private BigDecimal weight;

    private String dimensions;
    private Map<String, Object> customAttributes;
    private List<String> tags;
    private Boolean active;
}

@Data
@Builder
public class ProductAvailabilityDto {
    private Long productId;
    private Boolean available;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer lowStockThreshold;
    private Boolean isLowStock;
    private LocalDateTime nextRestockDate;
    private Integer estimatedDeliveryDays;
}

@Data
@Builder
public class InventoryUpdateDto {
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Min(value = 0, message = "Reserved quantity cannot be negative")
    private Integer reservedQuantity;

    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold;

    private LocalDateTime nextRestockDate;

    @Min(value = 1, message = "Estimated delivery days must be at least 1")
    private Integer estimatedDeliveryDays;
}

// ================== CATEGORY DTOs ==================

package com.sayantan.productservices.dto;

import lombok.Builder;
import lombok.Data;
import javax.validation.constraints.*;
        import java.time.LocalDateTime;

@Data
@Builder
public class CategoryDto {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@Builder
public class CategoryCreateDto {
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Long parentId;
}
