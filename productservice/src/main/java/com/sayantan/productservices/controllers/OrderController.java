package com.sayantan.productservices.controllers;

import com.sayantan.productservices.commons.AuthenticationCommons;
import com.sayantan.productservices.dto.*;
import com.sayantan.productservices.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * OrderController: Processes order creation and management requests
 * Responsible for checkout processing, order tracking, payment handling, and order history
 */
@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    private final AuthenticationCommons authenticationCommons;

    @Autowired
    public OrderController(OrderService orderService, AuthenticationCommons authenticationCommons) {
        this.orderService = orderService;
        this.authenticationCommons = authenticationCommons;
    }

    // ================== ORDER CREATION & CHECKOUT ==================

    /**
     * Process checkout and create order
     */
    @PostMapping("/checkout")
    public ResponseEntity<OrderDto> processCheckout(
            @Valid @RequestBody CheckoutDto checkoutDto,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            OrderDto order = orderService.processCheckout(checkoutDto, token);
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Create order from cart
     */
    @PostMapping("/from-cart")
    public ResponseEntity<OrderDto> createOrderFromCart(
            @Valid @RequestBody OrderFromCartDto orderDto,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            OrderDto order = orderService.createOrderFromCart(orderDto, token);
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Create direct order (buy now)
     */
    @PostMapping("/direct")
    public ResponseEntity<OrderDto> createDirectOrder(
            @Valid @RequestBody DirectOrderDto directOrderDto,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            OrderDto order = orderService.createDirectOrder(directOrderDto, token);
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // ================== ORDER RETRIEVAL ==================

    /**
     * Get user's order history with pagination
     */
    @GetMapping
    public ResponseEntity<Page<OrderSummaryDto>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        try {
            Page<OrderSummaryDto> orders = orderService.getUserOrders(token, status, pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get specific order details
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token) {

        if (!authenticationCommons.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            OrderDto order = orderService.getOrder(orderId, token);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // ================== ORDER TRACKING ==================

    /**
     * Track order status and shipment
     */
    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<OrderTrackingDto> trackOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token) {

        if (!auth