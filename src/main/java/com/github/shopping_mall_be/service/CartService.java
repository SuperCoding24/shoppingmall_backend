package com.github.shopping_mall_be.service;

import com.github.shopping_mall_be.domain.CartItem;
import com.github.shopping_mall_be.domain.Product;
import com.github.shopping_mall_be.domain.UserEntity;
import com.github.shopping_mall_be.dto.CartItemDto;
import com.github.shopping_mall_be.repository.CartItemRepository;
//import com.github.shopping_mall_be.repository.CartRepository;
import com.github.shopping_mall_be.repository.ProductRepository;
import com.github.shopping_mall_be.repository.User.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    // 장바구니에 아이템 추가
    public void addItemToCart(String email, Long productId, Integer quantity) {
        UserEntity user = userRepository.findByEmail2(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // `productStatus`가 1인 제품만 장바구니에 추가
        if (product.getProductStatus() == 1) {
            // 사용자의 카트에서 해당 productId를 가진 아이템이 이미 존재하는지 확인
            Optional<CartItem> existingCartItem = cartItemRepository.findByUserAndProduct(user, product);

            if (existingCartItem.isPresent()) {
                // 이미 존재한다면, 추가하지 않고 예외를 던짐
                throw new RuntimeException("해당 물건이 이미 장바구니 내부에 있습니다.");
            } else {
                // 존재하지 않는다면, 새로운 카트 아이템을 추가
                CartItem cartItem = new CartItem();
                cartItem.setUser(user);
                cartItem.setProduct(product);
                cartItem.setQuantity(quantity);
                cartItem.setCreatedAt(new Date());
                cartItem.setUpdatedAt(new Date());

                cartItemRepository.save(cartItem);
            }
        } else {
            // 제품의 상태가 1이 아닐 경우, 예외를 던짐
            throw new RuntimeException("판매중인 물건이 아닙니다.");
        }
    }


    public List<CartItem> getAllCartItems() {
        return cartItemRepository.findAll();
    }



    // 장바구니 아이템 총 가격 계산
    public Integer calculateTotalPrice(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserUserId(userId);
        int totalPrice = 0;

        for (CartItem item : cartItems) {
            // productStatus가 1인 제품만을 고려
            if (item.getProduct().getProductStatus() == 1) {
                totalPrice += item.getProduct().getPrice() * item.getQuantity();
            }
        }

        return totalPrice;
    }

    public void updateCartItem(Long cartItemId, CartItemDto cartItemDto) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findByCartItemId(cartItemId)
                .stream()
                .findFirst();

        // Optional<CartItem>이 비어있지 않은 경우 처리
        CartItem cartItem = cartItemOptional.orElseThrow(() -> new RuntimeException("CartItem not found"));

        // productId와 quantity가 제공되면 업데이트
        if (cartItemDto.getProductId() != null) {
            Product product = productRepository.findById(cartItemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // productStatus가 1인지 확인
            if (product.getProductStatus() != 1) {
                throw new RuntimeException("판매중인 상품만 업데이트 가능합니다. ");
            }

            cartItem.setProduct(product);
        }
        if (cartItemDto.getQuantity() != null) {
            cartItem.setQuantity(cartItemDto.getQuantity());
        }

        cartItem.setUpdatedAt(new Date());
        cartItemRepository.save(cartItem);

    }

    @Transactional
    public void deleteItemFromCartById(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public List<CartItemDto> getCartItemsByUserId(Long userId) {
        return cartItemRepository.findByUserUserId(userId).stream().map(cartItem -> {
            CartItemDto dto = new CartItemDto();
            dto.setProductId(cartItem.getProduct().getProductId());
            dto.setProductName(cartItem.getProduct().getProductName());
            dto.setQuantity(cartItem.getQuantity());
            dto.setPrice(cartItem.getProduct().getPrice());
            dto.setUserNickname(cartItem.getUser().getUser_nickname());
            dto.setCartItemId(cartItem.getCartItemId());
            dto.setUserId(cartItem.getUser().getUserId());
            dto.setProductOption(cartItem.getProduct().getProductOption());
            dto.setProductStatus(cartItem.getProduct().getProductStatus());
            dto.setStock(cartItem.getProduct().getStock());
            return dto;
        }).collect(Collectors.toList());
    }

}