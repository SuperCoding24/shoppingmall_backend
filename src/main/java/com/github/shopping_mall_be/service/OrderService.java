package com.github.shopping_mall_be.service;

import com.github.shopping_mall_be.domain.CartItem;
import com.github.shopping_mall_be.domain.OrderedItem;
import com.github.shopping_mall_be.domain.Product;
import com.github.shopping_mall_be.domain.UserEntity;
import com.github.shopping_mall_be.dto.OrderItemDto;
import com.github.shopping_mall_be.repository.CartItemRepository;
import com.github.shopping_mall_be.repository.OrderedItemRepository;
import com.github.shopping_mall_be.repository.ProductRepository;
import com.github.shopping_mall_be.repository.User.UserJpaRepository;
import com.github.shopping_mall_be.repository.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderedItemRepository orderedItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    public void createOrdersFromUserId(Long userId) {
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUserUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("No CartItems found for userId: " + userId);
        }

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProduct().getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getProductStatus() != 1) {
                throw new RuntimeException("구매하려는 상품이 구매 가능한 상태가 아닙니다.");
            }


            int newStock = product.getStock() - cartItem.getQuantity();
            if (newStock < 0) {
                throw new RuntimeException("구매하려는 상품의 재고가 충분하지 않습니다.");
            }

            OrderedItem orderedItem = new OrderedItem();
            orderedItem.setUser(user);
            orderedItem.setProduct(cartItem.getProduct());
            orderedItem.setQuantity(cartItem.getQuantity());
            orderedItem.setDescription(product.getDescription());
            orderedItem.setPrice(product.getPrice());
            orderedItem.setStock(newStock); // 주문 후 예상 재고를 설정

            int totalPrice = product.getPrice() * cartItem.getQuantity();
            orderedItem.setTotalPrice(totalPrice);

            orderedItemRepository.save(orderedItem);

            // 주문이 성공적으로 저장된 후에만 실제 상품의 재고를 감소시킵니다.
            product.setStock(newStock);
            productRepository.save(product);
        }
    }

    public void createOrderFromCartItemId(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        Product product = productRepository.findById(cartItem.getProduct().getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getProductStatus() != 1) {
            throw new RuntimeException("구매하려는 상품이 구매 가능한 상태가 아닙니다.");
        }

        int newStock = product.getStock() - cartItem.getQuantity();
        if (newStock < 0) {
            throw new RuntimeException("구매하려는 상품의 재고가 충분하지 않습니다.");
        }

        OrderedItem orderedItem = new OrderedItem();
        orderedItem.setUser(cartItem.getUser());
        orderedItem.setProduct(cartItem.getProduct());
        orderedItem.setQuantity(cartItem.getQuantity());
        orderedItem.setDescription(product.getDescription());
        orderedItem.setPrice(product.getPrice());
        orderedItem.setStock(newStock);

        int totalPrice = product.getPrice() * cartItem.getQuantity();
        orderedItem.setTotalPrice(totalPrice);

        orderedItemRepository.save(orderedItem);

        product.setStock(newStock);
        productRepository.save(product);
    }

    public void deleteItemFromOrderById(Long orderedItemId, String email, String password) {
        // 사용자 찾기 (이메일로 조회)
        UserEntity user = userRepository.findByEmail2(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getUser_password())) {
            throw new RuntimeException("Incorrect password.");
        }

        OrderedItem orderedItem = orderedItemRepository.findById(orderedItemId)
                .orElseThrow(() -> new RuntimeException("OrderedItem not found"));

        // 주문 항목과 연관된 상품의 소유자 확인
        Product product = orderedItem.getProduct();
        if (!product.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You do not have permission to delete this order item.");
        }

        // OrderedItem과 연관된 Product의 재고를 업데이트
        int returnedQuantity = orderedItem.getQuantity();
        int currentStock = product.getStock();
        product.setStock(currentStock + returnedQuantity);

        // 상품 재고 업데이트
        productRepository.save(product);

        // 주문 항목 삭제
        orderedItemRepository.delete(orderedItem);
    }

    public List<OrderItemDto> findByUserUserId(Long userId) {
        List<OrderedItem> orderedItems = orderedItemRepository.findByUserUserId(userId);
        return orderedItems.stream()
                .map(orderedItem -> {
                    Product product = orderedItem.getProduct();
                    // ProductRepository에서 상품의 최신 재고 정보를 가져옵니다.
                    Product latestProductInfo = productRepository.findById(product.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found"));
                    // OrderItemDto 생성자에 상품의 최신 재고 정보를 포함하여 객체를 생성합니다.
                    return new OrderItemDto(orderedItem, latestProductInfo.getStock());
                })
                .collect(Collectors.toList());
    }

}
