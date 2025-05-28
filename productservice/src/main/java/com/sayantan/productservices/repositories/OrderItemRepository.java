package com.sayantan.productservices.repositories;

import com.sayantan.productservices.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}