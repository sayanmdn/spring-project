package com.sayantan.productservices.services;

import com.sayantan.productservices.commons.AuthenticationCommons;
import com.sayantan.productservices.dto.*;
import com.sayantan.productservices.models.Cart;
import com.sayantan.productservices.models.Order;
import com.sayantan.productservices.models.OrderItem;
import com.sayantan.productservices.models.Product;
import com.sayantan.productservices.repositories.CartRepository;
import com.sayantan.productservices.repositories.OrderRepository;
import com.sayantan.productservices.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderService: Implements business logic for order creation and management
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AuthenticationCommons authenticationCommons;
    private final CartService cartService; // For cart-related operations

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        ProductRepository productRepository,
                        AuthenticationCommons authenticationCommons,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.authenticationCommons = authenticationCommons;
        this.cartService = cartService;
    }

    // ================== ORDER CREATION & CHECKOUT ==================

    @Transactional
    public OrderDto processCheckout(CheckoutDto checkoutDto, String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        validateCartItems(cart);

        Order order = createOrderFromCart(cart, checkoutDto);
        order.setUserId(userId);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        // Process payment (placeholder for payment gateway integration)
        processPayment(checkoutDto.getPaymentDetails());

        orderRepository.save(order);
        cartService.clearCart(token); // Clear cart after successful checkout

        return convertToOrderDto(order);
    }

    @Transactional
    public OrderDto createOrderFromCart(OrderFromCartDto orderDto, String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        validateCartItems(cart);

        Order order = createOrderFromCart(cart, orderDto);
        order.setUserId(userId);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        // Process payment (placeholder for payment gateway integration)
        processPayment(orderDto.getPaymentDetails());

        orderRepository.save(order);
        cartService.clearCart(token); // Clear cart after successful order creation

        return convertToOrderDto(order);
    }

    @Transactional
    public OrderDto createDirectOrder(DirectOrderDto directOrderDto, String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Product product = productRepository.findById(directOrderDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.isAvailable() || product.getStockQuantity() < directOrderDto.getQuantity()) {
            throw new RuntimeException("Product not available or insufficient stock");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        order.setShippingAddress(directOrderDto.getShippingAddress());
        order.setBillingAddress(directOrderDto.getBillingAddress());

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(directOrderDto.getQuantity());
        orderItem.setPrice(product.getPrice());
        orderItem.setOrder(order);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);

        calculateOrderTotals(order);

        // Process payment (placeholder for payment gateway integration)
        processPayment(directOrderDto.getPaymentDetails());

        orderRepository.save(order);
        return convertToOrderDto(order);
    }

    // ================== ORDER RETRIEVAL ==================

    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> getUserOrders(String token, String status, Pageable pageable) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Page<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            orders = orderRepository.findByUserId(userId, pageable);
        }
        return orders.map(this::convertToOrderSummaryDto);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(Long orderId, String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Order not found or unauthorized"));
        return convertToOrderDto(order);
    }

    // ================== ORDER TRACKING ==================

    @Transactional(readOnly = true)
    public OrderTrackingDto trackOrder(Long orderId, String token) {
        String userId = authenticationCommons.getUserIdFromToken(token);
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Order not found or unauthorized"));

        OrderTrackingDto trackingDto = new OrderTrackingDto();
        trackingDto.setOrderId(orderId);
        trackingDto.setStatus(order.getStatus());
        trackingDto.setEstimatedDelivery(order.getEstimatedDelivery());
        trackingDto.setTrackingNumber(order.getTrackingNumber());
        trackingDto.setShippingCarrier(order.getShippingCarrier());
        trackingDto.setUpdates(getTrackingUpdates(order));

        return trackingDto;
    }

    // ================== HELPER METHODS ==================

    private Order createOrderFromCart(Cart cart, Object checkoutDto) {
        Order order = new Order();
        order.setOrderItems(cart.getItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getProduct().getPrice());
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .collect(Collectors.toList()));

        if (checkoutDto instanceof CheckoutDto) {
            CheckoutDto dto = (CheckoutDto) checkoutDto;
            order.setShippingAddress(dto.getShippingAddress());
            order.setBillingAddress(dto.getBillingAddress());
        } else if (checkoutDto instanceof OrderFromCartDto) {
            OrderFromCartDto dto = (OrderFromCartDto) checkoutDto;
            order.setShippingAddress(dto.getShippingAddress());
            order.setBillingAddress(dto.getBillingAddress());
        }

        calculateOrderTotals(order);
        return order;
    }

    private void calculateOrderTotals(Order order) {
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Placeholder for tax and shipping calculations
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1)); // 10% tax rate
        BigDecimal shipping = BigDecimal.valueOf(10.0); // Flat shipping rate

        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setShippingCost(shipping);
        order.setTotal(subtotal.add(tax).add(shipping));
    }

    private void validateCartItems(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.isAvailable() || product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Product " + product.getName() + " is out of stock or insufficient quantity");
            }
        }
    }

    private void processPayment(PaymentDetailsDto paymentDetails) {
        // Placeholder for payment processing logic
        // Integrate with payment gateway (e.g., Stripe, PayPal)
        // Throw exception if payment fails
    }

    private List<String> getTrackingUpdates(Order order) {
        // Placeholder for tracking updates
        List<String> updates = new ArrayList<>();
        updates.add("Order created at " + order.getCreatedAt());
        if (order.getStatus().equals("SHIPPED")) {
            updates.add("Order shipped at " + order.getUpdatedAt());
        }
        return updates;
    }

    private OrderDto convertToOrderDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setStatus(order.getStatus());
        dto.setSubtotal(order.getSubtotal());
        dto.setTax(order.getTax());
        dto.setShippingCost(order.getShippingCost());
        dto.setTotal(order.getTotal());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setBillingAddress(order.getBillingAddress());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setOrderItems(order.getOrderItems().stream()
                .map(this::convertToOrderItemDto)
                .collect(Collectors.toList()));
        return dto;
    }

    private OrderSummaryDto convertToOrderSummaryDto(Order order) {
        OrderSummaryDto dto = new OrderSummaryDto();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus());
        dto.setTotal(order.getTotal());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setItemCount(order.getOrderItems().size());
        return dto;
    }

    private OrderItemDto convertToOrderItemDto(OrderItem orderItem) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        return dto;
    }
}