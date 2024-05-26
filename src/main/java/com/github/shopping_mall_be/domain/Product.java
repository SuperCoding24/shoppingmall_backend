package com.github.shopping_mall_be.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
@Entity
@Table(name = "Product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY , cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "product_option",nullable = false)
    private String productOption;

    @Column(name = "product_status")
    private int productStatus;


    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @ElementCollection // 여러 이미지 경로를 저장하기 위해 사용. 컬렉션 매핑
    @CollectionTable(name = "ProductImage", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_path")
    private List<String> imagePaths = new ArrayList<>(); // 이미지 경로들을 저장하기 위한 리스트

    public boolean canAddImage() { // 이미지를 추가할 수 있는지 여부 확인. 최대 이미지 개수 초과하지않는지 확인.
        return imagePaths.size() < 10;
    }

    public void addImage(String imagePath) { // 상품에 이미지 추가. 추가 전에 canAddImage()메서드로 최대 이미지 개수 확인.
        if (canAddImage()) {
            imagePaths.add(imagePath);
        } else {
            throw new IllegalStateException("사진은 10개까지 등록 가능합니다.");
        }
    }

    public void clearImages() {
        this.imagePaths.clear();
    }


    // image_url 은 상품 등록시 첫번째로 올린 사진을 가져옴
    @Column(name = "image_url")
    private String imageUrl;



}