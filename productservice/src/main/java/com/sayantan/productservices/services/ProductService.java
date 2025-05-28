package com.sayantan.productservices.services;

import com.sayantan.productservices.dto.*;
import com.sayantan.productservices.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * ProductService: Business logic interface for product-related operations
 * Handles product search, discovery, CRUD operations, inventory management, and analytics
 */
public interface ProductService<ProductAnalyticsDto> {

    // ================== PRODUCT SEARCH AND DISCOVERY ==================

    /**
     * Get all products with pagination
     * @param pageable Pagination information
     * @return Page of products
     */
    Page<Product> getAllProducts(Pageable pageable);

    /**
     * Search products based on criteria
     * @param searchCriteria Search parameters
     * @param pageable Pagination information
     * @return Page of matching products
     */
    Page<Product> searchProducts(ProductSearchCriteria searchCriteria, Pageable pageable);

    /**
     * Get product recommendations for a user
     * @param userId User ID
     * @param limit Number of recommendations
     * @return List of recommended products
     */
    List<Product> getRecommendations(Long userId, int limit);

    /**
     * Get products by category
     * @param categoryId Category ID
     * @param subcategoryId Subcategory ID (optional)
     * @param pageable Pagination information
     * @return Page of products in category
     */
    Page<Product> getProductsByCategory(Long categoryId, Long subcategoryId, Pageable pageable);

    // ================== PRODUCT DETAILS ==================

    /**
     * Get single product by ID
     * @param id Product ID
     * @return Product details
     * @throws ProductNotFoundException if product not found
     */
    Product getSingleProduct(Long id);

    /**
     * Get product availability information
     * @param id Product ID
     * @return Product availability details
     * @throws ProductNotFoundException if product not found
     */
    <ProductAvailabilityDto> ProductAvailabilityDto getProductAvailability(Long id);

    // ================== PRODUCT MANAGEMENT ==================

    /**
     * Create a new product
     * @param productDto Product creation data
     * @return Created product
     * @throws ProductCreationException if creation fails
     */
    Product createProduct(ProductCreateDto productDto);

    /**
     * Update product information
     * @param id Product ID
     * @param productDto Updated product data
     * @return Updated product
     * @throws ProductNotFoundException if product not found
     */
    Product updateProduct(Long id, ProductUpdateDto productDto);

    /**
     * Partially update product
     * @param id Product ID
     * @param updates Map of field updates
     * @return Updated product
     * @throws ProductNotFoundException if product not found
     */
    Product patchProduct(Long id, Map<String, Object> updates);

    /**
     * Delete a product
     * @param id Product ID
     * @throws ProductNotFoundException if product not found
     */
    void deleteProduct(Long id);

    /**
     * Upload product images
     * @param id Product ID
     * @param files Image files
     * @return List of uploaded image URLs
     * @throws ProductNotFoundException if product not found
     * @throws ImageUploadException if upload fails
     */
    List<String> uploadProductImages(Long id, MultipartFile[] files);

    /**
     * Update product inventory
     * @param id Product ID
     * @param inventoryDto Inventory update data
     * @return Updated product
     * @throws ProductNotFoundException if product not found
     */
    <T> Product updateInventory(Long id, T inventoryDto);

    // ================== CATEGORY MANAGEMENT ==================

    /**
     * Get all categories
     * @return List of all categories
     */
    <CategoryDto> List<CategoryDto> getAllCategories();

    /**
     * Create new category
     * @param categoryDto Category creation data
     * @return Created category
     * @throws CategoryCreationException if creation fails
     */
    <CategoryDto> CategoryDto createCategory(CategoryCreateDto categoryDto);

    // ================== ANALYTICS AND REPORTING ==================

    /**
     * Get product analytics
     * @param productId Product ID (optional)
     * @param dateFrom Start date (optional)
     * @param dateTo End date (optional)
     * @return Product analytics data
     */
    ProductAnalyticsDto getProductAnalytics(Long productId, String dateFrom, String dateTo);
}