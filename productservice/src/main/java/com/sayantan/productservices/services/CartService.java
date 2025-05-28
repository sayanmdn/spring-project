package com.sayantan.productservices.services;

import com.sayantan.productservices.commons.AuthenticationCommons;
import com.sayantan.productservices.dto.*;
import com.sayantan.productservices.models.Cart;
import com.sayantan.productservices.models.CartItem;
import com.sayantan.productservices.models.Product;
import com.sayantan.productservices.repositories.CartRepository;
import com.sayantan.productservices.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * CartService: Implements business logic for shopping cart operations
 */
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AuthenticationCommons authenticationCommons;
    private final WishlistService wishlistService; // Assuming a WishlistService exists

    @Autowired
    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository,
                       AuthenticationCommons authenticationCommons,
                       WishlistService wishlistService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.authenticationCommons = authenticationCommons;
        this.wishlistService = wishlistService;
    }

    // ================== CART MANAGEMENT ==================

    @Transactional(readOnly = true)
    public ShoppingCartDto getCart(String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return convertToShoppingCartDto(cart);
    }

    @Transactional
    public CartItemDto addToCart(AddToCartDto addToCartDto, String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        Product product = productRepository.findById(addToCartDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(addToCartDto.getProductId()))
                .findFirst();

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + addToCartDto.getQuantity());
        } else {
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(addToCartDto.getQuantity());
            cartItem.setCart(cart);
            cart.getItems().add(cartItem);
        }

        cartRepository.save(cart);
        return convertToCartItemDto(cartItem);
    }

    @Transactional
    public CartItemDto updateCartItem(Long itemId, UpdateCartItemDto updateDto, String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cartItem.setQuantity(updateDto.getQuantity());
        cartRepository.save(cart);
        return convertToCartItemDto(cartItem);
    }

    @Transactional
    public void removeFromCart(Long itemId, String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().clear();
        cart.setCouponCode(null);
        cartRepository.save(cart);
    }

    // ================== CART CALCULATIONS ==================

    @Transactional(readOnly = true)
    public CartCalculationDto calculateCartTotal(CartCalculationRequestDto request, String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = calculateTax(subtotal, request.getTaxRate());
        BigDecimal shipping = calculateShipping(cart, request.getShippingMethod());
        BigDecimal discount = calculateDiscount(cart);

        CartCalculationDto calculation = new CartCalculationDto();
        calculation.setSubtotal(subtotal);
        calculation.setTax(tax);
        calculation.setShipping(shipping);
        calculation.setDiscount(discount);
        calculation.setTotal(subtotal.add(tax).add(shipping).subtract(discount));

        return calculation;
    }

    @Transactional
    public CartDto applyCoupon(ApplyCouponDto couponDto, String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // Validate coupon (implementation depends on coupon system)
        if (!isValidCoupon(couponDto.getCouponCode())) {
            throw new RuntimeException("Invalid coupon code");
        }

        cart.setCouponCode(couponDto.getCouponCode());
        cartRepository.save(cart);
        return convertToCartDto(cart);
    }

    @Transactional
    public CartDto removeCoupon(String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.setCouponCode(null);
        cartRepository.save(cart);
        return convertToCartDto(cart);
    }

    // ================== CART VALIDATION ==================

    @Transactional(readOnly = true)
    public CartValidationDto validateCart(String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartValidationDto validation = new CartValidationDto();
        validation.setIsValid(true);
        List<String> issues = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.isAvailable()) {
                validation.setIsValid(false);
                issues.add("Product " + product.getName() + " is out of stock");
            }
            if (item.getQuantity() > product.getStockQuantity()) {
                validation.setIsValid(false);
                issues.add("Insufficient stock for " + product.getName());
            }
        }

        validation.setIssues(issues);
        return validation;
    }

    // ================== BULK OPERATIONS ==================

    @Transactional
    public List<CartItemDto> addMultipleToCart(List<AddToCartDto> addToCartDtos, String token) {
        List<CartItemDto> addedItems = new ArrayList<>();
        for (AddToCartDto addToCartDto : addToCartDtos) {
            addedItems.add(addToCart(addToCartDto, token));
        }
        return addedItems;
    }

    @Transactional
    public List<CartItemDto> updateMultipleItems(List<BulkUpdateCartItemDto> updateDtos, String token) {
        List<CartItemDto> updatedItems = new ArrayList<>();
        for (BulkUpdateCartItemDto updateDto : updateDtos) {
            updatedItems.add(updateCartItem(updateDto.getItemId(),
                    new UpdateCartItemDto(updateDto.getQuantity()), token));
        }
        return updatedItems;
    }

    @Transactional
    public void removeMultipleFromCart(List<Long> itemIds, String token) {
        for (Long itemId : itemIds) {
            removeFromCart(itemId, token);
        }
    }

    // ================== WISHLIST INTEGRATION ==================

    @Transactional
    public void moveToWishlist(Long itemId, String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        wishlistService.addToWishlist(cartItem.getProduct().getId(), cartItem.getQuantity(), token);
        cart.getItems().remove(cartItem);
        cartRepository.save(cart);
    }

    @Transactional
    public CartItemDto moveFromWishlist(Long productId, Integer quantity, String token) {
        AddToCartDto addToCartDto = new AddToCartDto();
        addToCartDto.setProductId(productId);
        addToCartDto.setQuantity(quantity);

        CartItemDto cartItem = addToCart(addToCartDto, token);
        wishlistService.removeFromWishlist(productId, token);
        return cartItem;
    }

    // ================== CART SHARING ==================

    @Transactional
    public CartShareDto shareCart(ShareCartDto shareDto, String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        String shareId = UUID.randomUUID().toString();
        // Implement sharing logic (e.g., save shared cart state)
        CartShareDto shareResult = new CartShareDto();
        shareResult.setShareId(shareId);
        shareResult.setSharedWith(shareDto.getSharedWithUserId());
        shareResult.setShareLink(generateShareLink(shareId));

        return shareResult;
    }

    @Transactional(readOnly = true)
    public SharedCartDto getSharedCart(String shareId) {
        // Implement logic to retrieve shared cart
        // This might involve a separate repository for shared carts
        SharedCartDto sharedCart = new SharedCartDto();
        // Populate shared cart details
        return sharedCart;
    }

    // ================== CART ANALYTICS ==================

    @Transactional(readOnly = true)
    public CartAbandonmentDto getCartAbandonmentData(String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartAbandonmentDto abandonment = new CartAbandonmentDto();
        // Populate with analytics data (e.g., last modified, item count, etc.)
        abandonment.setItemCount(cart.getItems().size());
        abandonment.setLastModified(cart.getLastModified());
        return abandonment;
    }

    // ================== HELPER METHODS ==================

    private Cart createNewCart(String userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }

    private ShoppingCartDto convertToShoppingCartDto(Cart cart) {
        ShoppingCartDto dto = new ShoppingCartDto();
        dto.setCartId(cart.getId());
        dto.setItems(cart.getItems().stream()
                .map(this::convertToCartItemDto)
                .toList());
        return dto;
    }

    private CartItemDto convertToCartItemDto(CartItem cartItem) {
        CartItemDto dto = new CartItemDto();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setQuantity(cartItem.getQuantity());
        dto.setPrice(cartItem.getProduct().getPrice());
        return dto;
    }

    private CartDto convertToCartDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setCartId(cart.getId());
        dto.setItems(cart.getItems().stream()
                .map(this::convertToCartItemDto)
                .toList());
        dto.setCouponCode(cart.getCouponCode());
        return dto;
    }

    private BigDecimal calculateTax(BigDecimal subtotal, BigDecimal taxRate) {
        return subtotal.multiply(taxRate);
    }

    private BigDecimal calculateShipping(Cart cart, String shippingMethod) {
        // Implement shipping calculation logic
        return BigDecimal.ZERO; // Placeholder
    }

    private BigDecimal calculateDiscount(Cart cart) {
        // Implement discount calculation based on coupon
        return BigDecimal.ZERO; // Placeholder
    }

    private boolean isValidCoupon(String couponCode) {
        // Implement coupon validation logic
        return true; // Placeholder
    }

    private String generateShareLink(String shareId) {
        // Implement share link generation
        return "https://example.com/cart/shared/" + shareId; // Placeholder
    }
}