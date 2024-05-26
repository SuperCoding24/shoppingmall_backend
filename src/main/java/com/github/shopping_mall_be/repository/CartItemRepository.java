package com.github.shopping_mall_be.repository;

import com.github.shopping_mall_be.domain.CartItem;
import com.github.shopping_mall_be.domain.Product;
import com.github.shopping_mall_be.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserUserId(Long userId);
    Optional<CartItem> findByCartItemId(Long cartItemId);
    Optional<CartItem> findByUserAndProduct(UserEntity user, Product product);


}