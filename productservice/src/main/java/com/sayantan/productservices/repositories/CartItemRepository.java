package com.sayantan.productservices.repositories;

import com.sayantan.productservices.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
