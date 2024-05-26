

package com.github.shopping_mall_be.controller;

import com.github.shopping_mall_be.domain.CartItem;
import com.github.shopping_mall_be.dto.CartItemDto;
import com.github.shopping_mall_be.dto.CartTotalPriceDto;
import com.github.shopping_mall_be.service.CartService;
import com.github.shopping_mall_be.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "CartController", description = "장바구니 관리 API")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/cart")
    @Operation(summary = "장바구니에 상품 추가", description = "사용자의 장바구니에 상품을 추가합니다.")
    public ResponseEntity<?> addItemToCart(
            @Parameter(description = "장바구니에 추가할 상품 정보", required = true) @RequestBody CartItemDto cartItemDto,
            @AuthenticationPrincipal Principal principal) {
        String userEmail = principal.getName(); // Get the email of the logged-in user
        cartService.addItemToCart(userEmail, cartItemDto.getProductId(), cartItemDto.getQuantity());
        return ResponseEntity.ok("장바구니에 상품이 정상적으로 담겼습니다.");
    }

    @GetMapping("/cart/total-price/{userId}")
    @Operation(summary = "특정 사용자의 장바구니 총 금액 조회", description = "userId에 따른 특정 사용자의 장바구니에 담긴 모든 상품의 총 금액을 조회합니다.")
    public ResponseEntity<?> getTotalPrice(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId) {
        Integer totalPrice = cartService.calculateTotalPrice(userId);
        return ResponseEntity.ok(new CartTotalPriceDto(totalPrice));
    }

    @GetMapping("/cart")
    @Operation(summary = "모든 장바구니 항목 조회", description = "모든 사용자의 장바구니 항목을 조회합니다. 해당 요청은 구현시 테스트를 위해 만들었습니다.")
    public ResponseEntity<List<CartItemDto>> getAllCartItems() {
        List<CartItem> cartItems = cartService.getAllCartItems();
        List<CartItemDto> cartItemDtos = cartItems.stream()
                .map(item -> {
                    CartItemDto dto = new CartItemDto();
                    dto.setUserId(item.getUser().getUserId());
                    dto.setProductId(item.getProduct().getProductId());
                    dto.setQuantity(item.getQuantity());
                    dto.setCartItemId(item.getCartItemId());
                    dto.setUserNickname(item.getUser().getUser_nickname());
                    dto.setPrice(item.getProduct().getPrice());
                    dto.setProductName(item.getProduct().getProductName());
                    return dto;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(cartItemDtos);
    }

    @PutMapping("/cart/{cartItemId}")
    @Operation(summary = "장바구니 항목 수정", description = "cartItemId에 따라 장바구니에 담긴 특정 항목을 수정합니다.")
    public ResponseEntity<?> updateCartItem(
            @Parameter(description = "장바구니 항목 ID", required = true) @PathVariable Long cartItemId,
            @RequestBody CartItemDto cartItemDto) {
        cartService.updateCartItem(cartItemId, cartItemDto);
        return ResponseEntity.ok("장바구니 물품이 성공적으로 수정 되었습니다.");
    }

    @DeleteMapping("/cart/{cartItemId}")
    @Operation(summary = "장바구니 항목 삭제", description = "cartItemId에 따라 장바구니에 담긴 특정 항목을 삭제합니다.")
    public ResponseEntity<?> deleteItemFromCartById(
            @Parameter(description = "장바구니 항목 ID", required = true) @PathVariable Long cartItemId) {
        cartService.deleteItemFromCartById(cartItemId);
        return ResponseEntity.ok("장바구니에서 해당 물건이 삭제되었습니다.");
    }

    @GetMapping("/cart/{userId}")
    @Operation(summary = "특정 사용자의 장바구니 항목 조회", description = "userId에 따라 특정 사용자의 장바구니 항목을 조회합니다.")
    public List<CartItemDto> getCartItems(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId) {
        return cartService.getCartItemsByUserId(userId);
    }
}
