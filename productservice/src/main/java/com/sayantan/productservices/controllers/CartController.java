package com.sayantan.productservices.controllers;

import com.sayantan.productservices.commons.AuthenticationCommons;
import com.sayantan.productservices.dto.*;
import com.sayantan.productservices.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * CartController: Manages shopping cart operations and user interactions
 * Responsible for cart management, item operations, and cost calculations
 */
@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;
    private final AuthenticationCommons authenticationCommons;

    @Autowired
    public CartController(CartService cartService, AuthenticationCommons authenticationCommons) {
        this.cartService = cartService;
        this.authenticationCommons = authenticationCommons;
    }

    // ================== CART MANAGEMENT ==================

    /**
     * Get user's shopping cart
     */
    @GetMapping
    public ResponseEntity<ShoppingCartDto> getCart(@RequestHeader("Authorization") String token) {
        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            ShoppingCartDto cart = cartService.getCart(token);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Add product to cart
     */
    @PostMapping("/add")
    public ResponseEntity<CartItemDto> addToCart(
            @Valid @RequestBody AddToCartDto addToCartDto,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            CartItemDto cartItem = cartService.addToCart(addToCartDto, token);
            return ResponseEntity.ok(cartItem);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update item quantity in cart
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartItemDto> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemDto updateDto,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            CartItemDto updatedItem = cartService.updateCartItem(itemId, updateDto, token);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeFromCart(
            @PathVariable Long itemId,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            cartService.removeFromCart(itemId, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Clear entire cart
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@RequestHeader("Authorization") String token) {
        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            cartService.clearCart(token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // ================== CART CALCULATIONS ==================

    /**
     * Calculate cart total with taxes and shipping
     */
    @PostMapping("/calculate")
    public ResponseEntity<CartCalculationDto> calculateCartTotal(
            @Valid @RequestBody CartCalculationRequestDto calculationRequest,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            CartCalculationDto calculation = cartService.calculateCartTotal(calculationRequest, token);
            return ResponseEntity.ok(calculation);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Apply coupon code to cart
     */
    @PostMapping("/coupon")
    public ResponseEntity<CartDto> applyCoupon(
            @Valid @RequestBody ApplyCouponDto couponDto,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            CartDto updatedCart = cartService.applyCoupon(couponDto, token);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Remove coupon from cart
     */
    @DeleteMapping("/coupon")
    public ResponseEntity<CartDto> removeCoupon(@RequestHeader("Authorization") String token) {
        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            CartDto updatedCart = cartService.removeCoupon(token);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // ================== CART VALIDATION ==================

    /**
     * Validate cart items (check availability, prices, etc.)
     */
    @PostMapping("/validate")
    public ResponseEntity<CartValidationDto> validateCart(@RequestHeader("Authorization") String token) {
        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            CartValidationDto validation = cartService.validateCart(token);
            return ResponseEntity.ok(validation);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // ================== BULK OPERATIONS ==================

    /**
     * Add multiple products to cart
     */
    @PostMapping("/bulk-add")
    public ResponseEntity<List<CartItemDto>> addMultipleToCart(
            @Valid @RequestBody List<AddToCartDto> addToCartDtos,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<CartItemDto> cartItems = cartService.addMultipleToCart(addToCartDtos, token);
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update multiple cart items
     */
    @PutMapping("/bulk-update")
    public ResponseEntity<List<CartItemDto>> updateMultipleItems(
            @Valid @RequestBody List<BulkUpdateCartItemDto> updateDtos,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<CartItemDto> updatedItems = cartService.updateMultipleItems(updateDtos, token);
            return ResponseEntity.ok(updatedItems);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Remove multiple items from cart
     */
    @DeleteMapping("/bulk-remove")
    public ResponseEntity<Void> removeMultipleFromCart(
            @RequestBody List<Long> itemIds,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            cartService.removeMultipleFromCart(itemIds, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // ================== WISHLIST INTEGRATION ==================

    /**
     * Move item from cart to wishlist
     */
    @PostMapping("/items/{itemId}/move-to-wishlist")
    public ResponseEntity<Void> moveToWishlist(
            @PathVariable Long itemId,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            cartService.moveToWishlist(itemId, token);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Move item from wishlist to cart
     */
    @PostMapping("/move-from-wishlist/{productId}")
    public ResponseEntity<CartItemDto> moveFromWishlist(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            CartItemDto cartItem = cartService.moveFromWishlist(productId, quantity, token);
            return ResponseEntity.ok(cartItem);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // ================== CART SHARING ==================

    /**
     * Share cart with another user
     */
    @PostMapping("/share")
    public ResponseEntity<CartShareDto> shareCart(
            @Valid @RequestBody ShareCartDto shareDto,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            CartShareDto shareResult = cartService.shareCart(shareDto, token);
            return ResponseEntity.ok(shareResult);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get shared cart details
     */
    @GetMapping("/shared/{shareId}")
    public ResponseEntity<SharedCartDto> getSharedCart(@PathVariable String shareId) {
        try {
            SharedCartDto sharedCart = cartService.getSharedCart(shareId);
            return ResponseEntity.ok(sharedCart);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // ================== CART ANALYTICS ==================

    /**
     * Get cart abandonment data (for logged-in users)
     */
    @GetMapping("/analytics/abandonment")
    public ResponseEntity<CartAbandonmentDto> getCartAbandonmentData(
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            CartAbandonmentDto abandonment = cartService.getCartAbandonmentData(token);
            return ResponseEntity.ok(abandonment);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}