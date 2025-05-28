package com.sayantan.productservices.services.impl;

import com.sayantan.productservices.dto.*;
import com.sayantan.productservices.dto.ProductAnalyticsDto;
import com.sayantan.productservices.exceptions.*;
import com.sayantan.productservices.models.Product;
import com.sayantan.productservices.models.Category;
import com.sayantan.productservices.repositories.ProductRepository;
import com.sayantan.productservices.repositories.CategoryRepository;
import com.sayantan.productservices.services.ProductService;
import com.sayantan.productservices.services.FileStorageService;
import com.sayantan.productservices.services.RecommendationService;
import com.sayantan.productservices.specifications.ProductSpecifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProductService Implementation: Business logic for product-related operations
 */
@Service("selfProductService")
@Slf4j
@Transactional
public class ProductServiceImpl<ProductAnalyticsDto> implements ProductService<ProductAnalyticsDto> {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private final RecommendationService recommendationService;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              FileStorageService fileStorageService,
                              RecommendationService recommendationService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
        this.recommendationService = recommendationService;
    }

    // ================== PRODUCT SEARCH AND DISCOVERY ==================

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination: {}", pageable);
        try {
            return productRepository.findByDeletedFalse(pageable);
        } catch (Exception e) {
            log.error("Error fetching all products", e);
            throw new ProductServiceException("Failed to fetch products", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(ProductSearchCriteria searchCriteria, Pageable pageable) {
        log.info("Searching products with criteria: {}", searchCriteria);
        try {
            Specification<Product> spec = buildProductSpecification(searchCriteria);
            return productRepository.findAll(spec, pageable);
        } catch (Exception e) {
            log.error("Error searching products with criteria: {}", searchCriteria, e);
            throw new ProductServiceException("Failed to search products", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getRecommendations(Long userId, int limit) {
        log.info("Fetching recommendations for user: {} with limit: {}", userId, limit);
        try {
            return recommendationService.getRecommendationsForUser(userId, limit);
        } catch (Exception e) {
            log.error("Error fetching recommendations for user: {}", userId, e);
            throw new ProductServiceException("Failed to get recommendations", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategory(Long categoryId, Long subcategoryId, Pageable pageable) {
        log.info("Fetching products by category: {} and subcategory: {}", categoryId, subcategoryId);
        try {
            if (subcategoryId != null) {
                return productRepository.findByCategoryIdAndSubcategoryIdAndDeletedFalse(
                        categoryId, subcategoryId, pageable);
            } else {
                return productRepository.findByCategoryIdAndDeletedFalse(categoryId, pageable);
            }
        } catch (Exception e) {
            log.error("Error fetching products by category: {}", categoryId, e);
            throw new ProductServiceException("Failed to fetch products by category", e);
        }
    }

    // ================== PRODUCT DETAILS ==================

    @Override
    @Transactional(readOnly = true)
    public Product getSingleProduct(Long id) {
        log.info("Fetching product with ID: {}", id);
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductAvailabilityDto getProductAvailability(Long id) {
        log.info("Fetching availability for product ID: {}", id);
        Product product = getSingleProduct(id);

        return ProductAvailabilityDto.builder()
                .productId(id)
                .available(product.getQuantity() > 0)
                .quantity(product.getQuantity())
                .reservedQuantity(product.getReservedQuantity())
                .availableQuantity(product.getQuantity() - product.getReservedQuantity())
                .lowStockThreshold(product.getLowStockThreshold())
                .isLowStock(product.getQuantity() <= product.getLowStockThreshold())
                .nextRestockDate(product.getNextRestockDate())
                .estimatedDeliveryDays(product.getEstimatedDeliveryDays())
                .build();
    }

    // ================== PRODUCT MANAGEMENT ==================

    @Override
    public Product createProduct(ProductCreateDto productDto) {
        log.info("Creating new product: {}", productDto.getName());
        try {
            validateProductDto(productDto);

            Product product = Product.builder()
                    .name(productDto.getName())
                    .description(productDto.getDescription())
                    .price(productDto.getPrice())
                    .categoryId(productDto.getCategoryId())
                    .subcategoryId(productDto.getSubcategoryId())
                    .brand(productDto.getBrand())
                    .sku(generateSKU(productDto))
                    .quantity(productDto.getQuantity())
                    .lowStockThreshold(productDto.getLowStockThreshold())
                    .weight(productDto.getWeight())
                    .dimensions(productDto.getDimensions())
                    .customAttributes(productDto.getCustomAttributes())
                    .tags(productDto.getTags())
                    .active(true)
                    .deleted(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Product savedProduct = productRepository.save(product);
            log.info("Product created successfully with ID: {}", savedProduct.getId());
            return savedProduct;
        } catch (Exception e) {
            log.error("Error creating product: {}", productDto.getName(), e);
            throw new ProductCreationException("Failed to create product", e);
        }
    }

    @Override
    public Product updateProduct(Long id, ProductUpdateDto productDto) {
        log.info("Updating product with ID: {}", id);
        try {
            Product existingProduct = getSingleProduct(id);

            updateProductFields(existingProduct, productDto);
            existingProduct.setUpdatedAt(LocalDateTime.now());

            Product updatedProduct = productRepository.save(existingProduct);
            log.info("Product updated successfully with ID: {}", id);
            return updatedProduct;
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating product with ID: {}", id, e);
            throw new ProductUpdateException("Failed to update product", e);
        }
    }

    @Override
    public Product patchProduct(Long id, Map<String, Object> updates) {
        log.info("Patching product with ID: {} with updates: {}", id, updates.keySet());
        try {
            Product existingProduct = getSingleProduct(id);

            applyPatchUpdates(existingProduct, updates);
            existingProduct.setUpdatedAt(LocalDateTime.now());

            Product patchedProduct = productRepository.save(existingProduct);
            log.info("Product patched successfully with ID: {}", id);
            return patchedProduct;
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error patching product with ID: {}", id, e);
            throw new ProductUpdateException("Failed to patch product", e);
        }
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        try {
            Product product = getSingleProduct(id);
            product.setDeleted(true);
            product.setActive(false);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
            log.info("Product soft deleted successfully with ID: {}", id);
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting product with ID: {}", id, e);
            throw new ProductDeletionException("Failed to delete product", e);
        }
    }

    @Override
    public List<String> uploadProductImages(Long id, MultipartFile[] files) {
        log.info("Uploading {} images for product ID: {}", files.length, id);
        try {
            Product product = getSingleProduct(id);

            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String imageUrl = fileStorageService.storeFile(file, "products/" + id);
                    imageUrls.add(imageUrl);
                }
            }

            // Add new image URLs to existing ones
            List<String> existingImages = product.getImageUrls() != null ?
                    new ArrayList<>(product.getImageUrls()) : new ArrayList<>();
            existingImages.addAll(imageUrls);
            product.setImageUrls(existingImages);
            product.setUpdatedAt(LocalDateTime.now());

            productRepository.save(product);
            log.info("Successfully uploaded {} images for product ID: {}", imageUrls.size(), id);
            return imageUrls;
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error uploading images for product ID: {}", id, e);
            throw new ImageUploadException("Failed to upload product images", e);
        }
    }

    /**
     * Update product inventory
     *
     * @param id           Product ID
     * @param inventoryDto Inventory update data
     * @return Updated product
     * @throws ProductNotFoundException if product not found
     */
    @Override
    public Product updateInventory(Long id, Object inventoryDto) {
        return null;
    }

    @Override
    public <T> Product updateInventory(Long id, T inventoryDto) {
        log.info("Updating inventory for product ID: {}", id);
        try {
            Product product = getSingleProduct(id);

            // Use reflection to update inventory fields
            updateInventoryFields(product, inventoryDto);
            product.setUpdatedAt(LocalDateTime.now());

            Product updatedProduct = productRepository.save(product);
            log.info("Inventory updated successfully for product ID: {}", id);
            return updatedProduct;
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating inventory for product ID: {}", id, e);
            throw new InventoryUpdateException("Failed to update inventory", e);
        }
    }

    // ================== CATEGORY MANAGEMENT ==================

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        log.info("Fetching all categories");
        try {
            List<Category> categories = categoryRepository.findByDeletedFalse();
            return categories.stream()
                    .map(this::convertToCategoryDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching categories", e);
            throw new CategoryServiceException("Failed to fetch categories", e);
        }
    }

    @Override
    public CategoryDto createCategory(CategoryCreateDto categoryDto) {
        log.info("Creating new category: {}", categoryDto.getName());
        try {
            Category category = Category.builder()
                    .name(categoryDto.getName())
                    .description(categoryDto.getDescription())
                    .parentId(categoryDto.getParentId())
                    .active(true)
                    .deleted(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Category savedCategory = categoryRepository.save(category);
            log.info("Category created successfully with ID: {}", savedCategory.getId());
            return convertToCategoryDto(savedCategory);
        } catch (Exception e) {
            log.error("Error creating category: {}", categoryDto.getName(), e);
            throw new CategoryCreationException("Failed to create category", e);
        }
    }

    // ================== ANALYTICS AND REPORTING ==================

    @Override
    @Transactional(readOnly = true)
    public ProductAnalyticsDto getProductAnalytics(Long productId, String dateFrom, String dateTo) {
        log.info("Fetching analytics for product: {} from {} to {}", productId, dateFrom, dateTo);
        try {
            LocalDateTime fromDate = parseDate(dateFrom);
            LocalDateTime toDate = parseDate(dateTo);

            if (productId != null) {
                return getIndividualProductAnalytics(productId, fromDate, toDate);
            } else {
                return getOverallProductAnalytics(fromDate, toDate);
            }
        } catch (Exception e) {
            log.error("Error fetching product analytics", e);
            throw new AnalyticsException("Failed to fetch product analytics", e);
        }
    }

    // ================== PRIVATE HELPER METHODS ==================

    private Specification<Product> buildProductSpecification(ProductSearchCriteria criteria) {
        Specification<Product> spec = ProductSpecifications.notDeleted();

        if (criteria.getName() != null) {
            spec = spec.and(ProductSpecifications.nameContains(criteria.getName()));
        }
        if (criteria.getCategory() != null) {
            spec = spec.and(ProductSpecifications.categoryEquals(criteria.getCategory()));
        }
        if (criteria.getBrand() != null) {
            spec = spec.and(ProductSpecifications.brandEquals(criteria.getBrand()));
        }
        if (criteria.getMinPrice() != null) {
            spec = spec.and(ProductSpecifications.priceGreaterThanOrEqual(criteria.getMinPrice()));
        }
        if (criteria.getMaxPrice() != null) {
            spec = spec.and(ProductSpecifications.priceLessThanOrEqual(criteria.getMaxPrice()));
        }
        if (criteria.getAvailable() != null && criteria.getAvailable()) {
            spec = spec.and(ProductSpecifications.isAvailable());
        }
        if (criteria.getMinRating() != null) {
            spec = spec.and(ProductSpecifications.ratingGreaterThanOrEqual(criteria.getMinRating()));
        }

        return spec;
    }

    private void validateProductDto(ProductCreateDto productDto) {
        if (productDto.getName() == null || productDto.getName().trim().isEmpty()) {
            throw new ValidationException("Product name is required");
        }
        if (productDto.getPrice() == null || productDto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Product price must be greater than zero");
        }
        if (productDto.getCategoryId() == null) {
            throw new ValidationException("Category is required");
        }
    }

    private String generateSKU(ProductCreateDto productDto) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String namePrefix = productDto.getName().replaceAll("[^a-zA-Z0-9]", "")
                .toUpperCase().substring(0, Math.min(3, productDto.getName().length()));
        return namePrefix + "-" + timestamp.substring(timestamp.length() - 6);
    }

    private void updateProductFields(Product product, ProductUpdateDto productDto) {
        if (productDto.getName() != null) product.setName(productDto.getName());
        if (productDto.getDescription() != null) product.setDescription(productDto.getDescription());
        if (productDto.getPrice() != null) product.setPrice(productDto.getPrice());
        if (productDto.getBrand() != null) product.setBrand(productDto.getBrand());
        if (productDto.getQuantity() != null) product.setQuantity(productDto.getQuantity());
        if (productDto.getWeight() != null) product.setWeight(productDto.getWeight());
        if (productDto.getDimensions() != null) product.setDimensions(productDto.getDimensions());
        if (productDto.getCustomAttributes() != null) product.setCustomAttributes(productDto.getCustomAttributes());
        if (productDto.getTags() != null) product.setTags(productDto.getTags());
        if (productDto.getActive() != null) product.setActive(productDto.getActive());
    }

    private void applyPatchUpdates(Product product, Map<String, Object> updates) {
        updates.forEach((key, value) -> {
            try {
                Field field = Product.class.getDeclaredField(key);
                field.setAccessible(true);
                field.set(product, value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.warn("Failed to update field: {} with value: {}", key, value);
            }
        });
    }

    private <T> void updateInventoryFields(Product product, T inventoryDto) {
        try {
            Field[] fields = inventoryDto.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(inventoryDto);
                if (value != null) {
                    Field productField = Product.class.getDeclaredField(field.getName());
                    productField.setAccessible(true);
                    productField.set(product, value);
                }
            }
        } catch (Exception e) {
            throw new InventoryUpdateException("Failed to update inventory fields", e);
        }
    }

    private CategoryDto convertToCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}, using default format", dateStr);
            return LocalDateTime.parse(dateStr + "T00:00:00");
        }
    }

    private ProductAnalyticsDto getIndividualProductAnalytics(Long productId, LocalDateTime fromDate, LocalDateTime toDate) {
        // Implementation for individual product analytics
        Product product = getSingleProduct(productId);

        return ProductAnalyticsDto.builder()
                .productId(productId)
                .productName(product.getName())
                .totalViews(0L) // Would be fetched from analytics service
                .totalSales(0L)
                .revenue(BigDecimal.ZERO)
                .conversionRate(0.0)
                .averageRating(product.getAverageRating())
                .totalReviews(product.getReviewCount())
                .currentStock(product.getQuantity())
                .dateFrom(fromDate)
                .dateTo(toDate)
                .build();
    }

    private ProductAnalyticsDto getOverallProductAnalytics(LocalDateTime fromDate, LocalDateTime toDate) {
        // Implementation for overall analytics
        long totalProducts = productRepository.countByDeletedFalse();

        return ProductAnalyticsDto.builder()
                .totalProducts(totalProducts)
                .totalViews(0L) // Would be fetched from analytics service
                .totalSales(0L)
                .revenue(BigDecimal.ZERO)
                .dateFrom(fromDate)
                .dateTo(toDate)
                .build();
    }
}