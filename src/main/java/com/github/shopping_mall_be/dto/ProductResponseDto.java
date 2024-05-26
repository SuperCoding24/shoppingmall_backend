package com.github.shopping_mall_be.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// 전체 product 조회용 dto

public class ProductResponseDto {

    private Long productId;
    private String productName;
    private String description;
    private Integer price;
    private Integer stock;
    private String imageUrl;
    private String userNickName;
    private String productOption;
    private Integer productStatus;
    private Date startDate;
    private Date endDate;


}
