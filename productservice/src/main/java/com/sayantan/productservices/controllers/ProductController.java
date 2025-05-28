package com.sayantan.productservices.controllers;

import com.sayantan.productservices.commons.AuthenticationCommons;
import com.sayantan.productservices.dto.*;
import com.sayantan.productservices.models.Product;
import com.sayantan.productservices.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ProductController: Handles HTTP requests and responses for product-related operations
 * Responsible for product search, discovery, CRUD operations, and inventory management
 */
@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;
    private final RestTemplate restTemplate;
    private final AuthenticationCommons authenticationCommons;

    @Autowired
    public ProductController(@Qualifier("selfProductService") ProductService productService,
                             RestTemplate restTemplate,
                             AuthenticationCommons authenticationCommons) {
        this.productService = productService;
        this.restTemplate = restTemplate;
        this.authenticationCommons = authenticationCommons;
    }

    // ================== PRODUCT SEARCH AND DISCOVERY ==================

    /**
     * Get all products with pagination and sorting
     */
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productService.getAllProducts(pageable);

        return ResponseEntity.ok(products);
    }

    /**
     * Search products by name, category, brand with advanced filtering
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Map<String, String> customAttributes,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        ProductSearchCriteria searchCriteria = ProductSearchCriteria.builder()
                .name(name)
                .category(category)
                .brand(brand)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .available(available)
                .minRating(minRating)
                .customAttributes(customAttributes)
                .build();

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> searchResults = productService.searchProducts(searchCriteria, pageable);

        return ResponseEntity.ok(searchResults);
    }

    /**
     * Get product recommendations for a user
     */
    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<List<Product>> getProductRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {

        List<Product> recommendations = productService.getRecommendations(userId, limit);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Get products by category with subcategory support
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false) Long subcategoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productService.getProductsByCategory(categoryId, subcategoryId, pageable);

        return ResponseEntity.ok(products);
    }

    // ================== PRODUCT DETAILS ==================

    /**
     * Get single product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable("id") Long id) {
        try {
            Product product = productService.getSingleProduct(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get product availability and inventory
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<ProductAvailabilityDto> getProductAvailability(@PathVariable Long id) {
        try {
            ProductAvailabilityDto availability = productService.getProductAvailability(id);
            return ResponseEntity.ok(availability);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // ================== PRODUCT MANAGEMENT (ADMIN ONLY) ==================

    /**
     * Create a new product (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(
            @Valid @RequestBody ProductCreateDto productDto,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Product createdProduct = productService.createProduct(productDto);
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update product information (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductUpdateDto productDto,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Product updatedProduct = productService.updateProduct(id, productDto);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Partially update product (Admin only)
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> patchProduct(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> updates,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Product patchedProduct = productService.patchProduct(id, updates);
            return ResponseEntity.ok(patchedProduct);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Delete a product (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            productService.deleteProduct(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Upload product images (Admin only)
     */
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> uploadProductImages(
            @PathVariable Long id,
            @RequestParam("files") MultipartFile[] files,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<String> imageUrls = productService.uploadProductImages(id, files);
            return ResponseEntity.ok(imageUrls);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update product inventory (Admin only)
     */
    @PatchMapping("/{id}/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateInventory(
            @PathVariable Long id,
            @RequestBody InventoryUpdateDto inventoryDto,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Product updatedProduct = productService.updateInventory(id, inventoryDto);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // ================== CATEGORY MANAGEMENT ==================

    /**
     * Get all categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Create new category (Admin only)
     */
    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CategoryCreateDto categoryDto,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            CategoryDto category = productService.createCategory(categoryDto);
            return new ResponseEntity<>(category, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // ================== ANALYTICS AND REPORTING ==================

    /**
     * Get product analytics (Admin only)
     */
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductAnalyticsDto> getProductAnalytics(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            ProductAnalyticsDto analytics = productService.getProductAnalytics(productId, dateFrom, dateTo);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}