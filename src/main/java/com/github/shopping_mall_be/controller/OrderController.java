package com.github.shopping_mall_be.controller;

import com.github.shopping_mall_be.domain.OrderedItem;
import com.github.shopping_mall_be.dto.OrderItemDto;
import com.github.shopping_mall_be.repository.OrderedItemRepository;
import com.github.shopping_mall_be.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Order Controller", description = "주문 관련 API")
public class OrderController {

    private final OrderedItemRepository orderedItemRepository;

    @Autowired
    public OrderController(OrderedItemRepository orderedItemRepository) {
        this.orderedItemRepository = orderedItemRepository;
    }

    @Autowired
    private OrderService orderService;

    @PostMapping("/ordertotal/{userId}")
    @Operation(summary = "카트의 아이템으로 주문 생성", description = "사용자 ID를 통해 카트의 모든 아이템으로 주문을 생성합니다.")
    public ResponseEntity<?> orderItemsFromCart(@Parameter(description = "사용자 ID", required = true) @PathVariable Long userId) {
        orderService.createOrdersFromUserId(userId);
        return ResponseEntity.ok("상품이 성공적으로 구매되었습니다.");
    }

    @DeleteMapping("/order/{orderedItemId}")
    @Operation(summary = "주문 아이템 삭제", description = "주문된 아이템을 삭제합니다.")
    public ResponseEntity<?> deleteOrderItem(@Parameter(description = "주문된 아이템 ID", required = true) @PathVariable Long orderedItemId,
                                             @Parameter(description = "이메일", required = true) @RequestParam String email,
                                             @Parameter(description = "비밀번호", required = true) @RequestParam String password) {
        orderService.deleteItemFromOrderById(orderedItemId, email, password);
        return ResponseEntity.ok("해당 구매 물건이 삭제되었습니다.");
    }

    @PostMapping("/order/{cartItemId}")
    @Operation(summary = "카트 아이템으로 주문 생성", description = "카트 아이템 ID를 이용하여 주문을 생성합니다.")
    public ResponseEntity<String> createOrder(@Parameter(description = "카트 아이템 ID", required = true) @PathVariable Long cartItemId) {
        try {
            orderService.createOrderFromCartItemId(cartItemId);
            return ResponseEntity.ok("주문이 성공적으로 완료되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/order/{userId}")
    @Operation(summary = "사용자별 주문 아이템 조회", description = "사용자 ID로 주문된 모든 아이템을 조회합니다.")
    public List<OrderItemDto> getOrderItemsByUserId(@Parameter(description = "사용자 ID", required = true) @PathVariable Long userId) {
        return orderService.findByUserUserId(userId);
    }
}