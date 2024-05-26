
package com.github.shopping_mall_be.controller;

import com.github.shopping_mall_be.domain.Product;
import com.github.shopping_mall_be.dto.DetailProductDto;
import com.github.shopping_mall_be.dto.Jwt.JwtProvider;
import com.github.shopping_mall_be.dto.ProductDTO;
import com.github.shopping_mall_be.dto.ProductResponseDto;
import com.github.shopping_mall_be.repository.User.UserRepository;
import com.github.shopping_mall_be.service.ProductService;
import com.github.shopping_mall_be.util.FileStorageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Product Controller", description = "상품 관련 API")
@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private FileStorageUtil fileStorageUtil;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/products")
    @Operation(summary = "상품 목록 조회", description = "페이지와 정렬 방식에 따른 상품 목록을 조회합니다.")
    public List<ProductResponseDto> getProducts(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 당 상품 수") @RequestParam(defaultValue = "8") int size,
            @Parameter(description = "정렬 방식 (asc: 오름차순, desc: 내림차순)") @RequestParam(defaultValue = "") String sort) {
        return productService.getAvailableProducts(page, size, sort);
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "상품 상세 조회", description = "상품 ID에 해당하는 상품의 상세 정보를 조회합니다.")
    public DetailProductDto getProductById(@Parameter(description = "상품 ID") @PathVariable Long productId) {
        return productService.getProductById(productId);
    }

    @GetMapping("/products/user/{userId}")
    @Operation(summary = "사용자 ID로 상품 조회", description = "사용자 ID에 해당하는 사용자가 등록한 상품 목록을 조회합니다.")
    public List<ProductResponseDto> getProductsByUserId(@Parameter(description = "사용자 ID") @PathVariable Long userId) {
        return productService.getProductsByUserId(userId);
    }

    @PostMapping("/products/register")
    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    public ProductDTO registerProduct(@ModelAttribute ProductDTO productDTO, @RequestParam("files") List<MultipartFile> files, Principal principal) throws IOException {
        String userEmail = principal.getName();

        productDTO.setFiles(files);

        ProductDTO registeredProduct = productService.registerProduct(userEmail, productDTO);

        return registeredProduct;
    }

    @PutMapping("/products/{productId}")
    @Operation(summary = "상품 정보 수정", description = "상품 ID에 해당하는 상품의 정보를 수정합니다.")
    public ResponseEntity<?> updateProduct(
            @Parameter(description = "상품 ID") @PathVariable Long productId,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("productName") String productName,
            @RequestParam("price") int price,
            @RequestParam("stock") int stock,
            @RequestParam("productOption") String productOption,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
            @RequestParam("description") String description,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setProductName(productName);
            productDTO.setPrice(price);
            productDTO.setStock(stock);
            productDTO.setProductOption(productOption);
            productDTO.setStartDate(startDate);
            productDTO.setEndDate(endDate);
            productDTO.setDescription(description);
            productDTO.setFiles(files);

            ProductDTO updatedProduct = productService.updateProduct(productId, email, productDTO, password);
            return ResponseEntity.ok(updatedProduct);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("파일 저장 중 오류가 발생했습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("상품 정보 업데이트 중 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/products/{productId}")
    @Operation(summary = "상품 삭제", description = "상품 ID에 해당하는 상품을 삭제합니다.")
    public String deleteProduct(@Parameter(description = "상품 ID") @PathVariable Long productId, @RequestParam String email, @RequestParam String password) {
        productService.deleteProduct(productId, email, password);
        return "해당 물건이 성공적으로 삭제 되었습니다.";
    }
}