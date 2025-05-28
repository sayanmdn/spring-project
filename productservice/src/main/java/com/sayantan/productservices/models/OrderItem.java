package com.sayantan.productservices.models;

import jakarta.persistence.*;
import jakarta.persistence.criteria.Order;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Product product;

    private int quantity;
    private double price;

    @ManyToOne
    private Order order;

    // Getters and setters
}
