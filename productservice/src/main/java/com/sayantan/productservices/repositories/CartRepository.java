package com.sayantan.productservices.repositories;

import com.sayantan.productservices.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
}