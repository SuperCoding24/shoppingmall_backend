package com.github.shopping_mall_be.repository;

import com.github.shopping_mall_be.domain.OrderedItem;
import com.github.shopping_mall_be.dto.OrderItemDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderedItemRepository extends JpaRepository<OrderedItem, Long> {
    List<OrderedItem> findByUserUserId(Long userId);
}
