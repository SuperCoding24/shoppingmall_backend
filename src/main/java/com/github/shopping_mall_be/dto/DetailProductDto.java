package com.github.shopping_mall_be.dto;

import com.github.shopping_mall_be.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

// 전체 product 조회용 dto

public class DetailProductDto {

    private Long productId;
    private String productName;
    private String description;
    private Integer price;
    private Integer stock;
    private String userNickName;
    private String productOption;
    private Date startDate;
    private Date endDate;
    private Integer productStatus;
    private List<String> imagePaths = new ArrayList<>();


    public DetailProductDto(Long productId, String productName, String description, Integer price, Integer stock, String userNickName, String productOption, Integer productStatus, Date startDate, Date endDate) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.userNickName = userNickName;
        this.productOption = productOption;
        this.productStatus = productStatus;
        this.startDate = startDate;
        this.endDate = endDate;
    }


}
