package com.github.shopping_mall_be.service;


import com.github.shopping_mall_be.domain.Product;
import com.github.shopping_mall_be.domain.UserEntity;
import com.github.shopping_mall_be.dto.DetailProductDto;
import com.github.shopping_mall_be.dto.ProductDTO;
import com.github.shopping_mall_be.dto.ProductResponseDto;
import com.github.shopping_mall_be.repository.ProductRepository;
import com.github.shopping_mall_be.repository.User.UserJpaRepository;
import com.github.shopping_mall_be.repository.User.UserRepository;
import com.github.shopping_mall_be.util.FileStorageUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private FileStorageUtil fileStorageUtil;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    public List<ProductResponseDto> getAvailableProducts(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productsPage = productRepository.findAllByStockGreaterThanAndProductStatus(0, 1, pageable);
// productStatus가 1인 물건만 조회가능
        List<ProductResponseDto> productDtos = productsPage.stream().map(product -> {
            ProductResponseDto dto = new ProductResponseDto();
            dto.setProductId(product.getProductId());
            dto.setProductName(product.getProductName());
            dto.setDescription(product.getDescription());
            dto.setPrice(product.getPrice());
            dto.setStartDate(product.getStartDate());
            dto.setEndDate(product.getEndDate());
            dto.setStock(product.getStock());
            dto.setProductOption(product.getProductOption());
            dto.setImageUrl(product.getImageUrl());
            dto.setProductStatus(product.getProductStatus());
            dto.setUserNickName(product.getUser().getUser_nickname());
            return dto;
        }).collect(Collectors.toList());

        if ("asc".equalsIgnoreCase(sort)) {
            productDtos.sort(Comparator.comparing(ProductResponseDto::getPrice));
        } else if ("desc".equalsIgnoreCase(sort)) {
            productDtos.sort(Comparator.comparing(ProductResponseDto::getPrice).reversed());
        }

        return productDtos;
    }


    public DetailProductDto getProductById(Long productId) {
        return productRepository.findById(productId)
                .filter(product -> product.getStock() > 0)
                .map(product -> new DetailProductDto(
                        product.getProductId(),
                        product.getProductName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getStock(),
                        product.getUser().getUser_nickname(),
                        product.getProductOption(),
                        product.getStartDate(),
                        product.getEndDate(),
                        product.getProductStatus(),
                        product.getImagePaths()
                ))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<ProductResponseDto> getProductsByUserId(Long userId){
        return productRepository.findByUserUserId(userId).stream()
                .filter(product -> product.getStock() > 0)
                .filter(product -> product.getProductStatus() == 1) // productStatus가 1인 제품만 필터링
                .sorted(Comparator.comparing(Product::getEndDate)) // endDate로 정렬
                .map(product -> new ProductResponseDto(
                        product.getProductId(),
                        product.getProductName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getStock(),
                        product.getImageUrl(),
                        product.getUser().getUser_nickname(),
                        product.getProductOption(),
                        product.getProductStatus(),
                        product.getStartDate(),
                        product.getEndDate()
                )).collect(Collectors.toList());
    }



    public ProductDTO registerProduct(String email,ProductDTO productDTO) throws IOException {
            validateProductInfo(productDTO);
            UserEntity user = userRepository.findByEmail2(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<MultipartFile> files = productDTO.getFiles() != null ? productDTO.getFiles() : new ArrayList<>();

            List<String> imagePaths = files.stream()
                    .map(file -> {
                        try {
                            return fileStorageUtil.storeFile(file);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
                        }
                    })
                    .collect(Collectors.toList());


            // 상품 정보 설정
            Product product = new Product();
            product.setProductName(productDTO.getProductName());
            product.setPrice(productDTO.getPrice());
            product.setDescription(productDTO.getDescription());
            product.setStock(productDTO.getStock());
            product.setStartDate(productDTO.getStartDate());
            product.setEndDate(productDTO.getEndDate());
            product.setDescription(productDTO.getDescription());
            product.setProductOption(productDTO.getProductOption());
            product.setUser(user);
            productDTO.setUserNickName(productDTO.getUserNickName());

            Date now = new Date();
            product.setProductStatus(product.getEndDate().compareTo(now) >= 0 ? 1 : 0);


            // 첫 번째 이미지 경로를 imageUrl에 설정
            if (!imagePaths.isEmpty()) {
                product.setImageUrl(imagePaths.get(0)); // 첫 번째 이미지 경로를 imageUrl에 설정
            }

            imagePaths.forEach(product::addImage);  // 각 이미지 경로를 Product에 추가
            // 상품 등록
            Product registeredProduct = productRepository.save(product);
            // 등록된 상품 정보를 DTO로 변환하여 반환
            return new ProductDTO(registeredProduct);
        }
    private void validateProductInfo(ProductDTO productDTO) {
        // 필요한 모든 상품 정보가 입력되었는지 확인하는 로직 추가
        // 예: productName, price, startDate, endDate, description 등
        // 필수 정보가 빠진 경우 예외를 던지도록 구현
        if (productDTO.getProductName() == null || productDTO.getProductName().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (productDTO.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (productDTO.getStartDate() == null || productDTO.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        if (productDTO.getDescription() == null || productDTO.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
    }

    public void deleteProduct(Long productId, String email, String password) {
        // 사용자 찾기 (이메일로 조회)
        UserEntity user = userRepository.findByEmail2(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getUser_password())) {
            throw new RuntimeException("Incorrect password.");
        }

        // 상품 찾기
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        // 상품 소유자 확인
        if (!product.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You do not have permission to delete this product.");
        }

        // 상품 삭제
        productRepository.deleteById(productId);
    }

    public ProductDTO updateProduct(Long productId, String email, ProductDTO productDTO, String password) throws IOException {
        // 사용자 찾기 (이메일 통해 조회)
        UserEntity user = userRepository.findByEmail2(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getUser_password())) {
            throw new RuntimeException("Incorrect password.");
        }

        // 상품 찾기
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 상품 소유자 확인
        if (!product.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You do not have permission to update this product.");
        }

        // 상품 정보 업데이트
        product.setProductName(productDTO.getProductName());
        product.setPrice(productDTO.getPrice());
        product.setDescription(productDTO.getDescription());
        product.setStock(productDTO.getStock());
        product.setStartDate(productDTO.getStartDate());
        product.setEndDate(productDTO.getEndDate());
        product.setProductOption(productDTO.getProductOption());

        Date now = new Date();
        product.setProductStatus(product.getEndDate().compareTo(now) >= 0 ? 1 : 0);

        // 이미지 처리
        List<MultipartFile> files = productDTO.getFiles() != null ? productDTO.getFiles() : new ArrayList<>();
        List<String> imagePaths = files.stream()
                .map(file -> {
                    try {
                        return fileStorageUtil.storeFile(file);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
                    }
                })
                .collect(Collectors.toList());

        // 기존 이미지 정보 제거 후 새 이미지 정보 추가
        product.clearImages();
        imagePaths.forEach(product::addImage);

        // 첫 번째 이미지를 메인 이미지로 설정
        if (!imagePaths.isEmpty()) {
            product.setImageUrl(imagePaths.get(0));
        }

        // 상품 정보 저장
        Product updatedProduct = productRepository.save(product);

        // 업데이트된 상품 정보를 DTO로 변환하여 반환
        return new ProductDTO(updatedProduct);
    }
}



