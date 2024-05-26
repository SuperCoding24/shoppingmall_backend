package com.github.shopping_mall_be.config;

import com.github.shopping_mall_be.domain.Product;
import com.github.shopping_mall_be.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

// 매일 endDate 체크하고 , productStatus 업데이트하는 스케줄러

@Component
public class ProductStatusScheduler {

    @Autowired
    private ProductRepository productRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateProductStatus() {
        List<Product> products = productRepository.findAll();
        Date now = new Date();
        products.forEach(product -> {
            // endDate가 null이 아니며, endDate가 현재 날짜와 같거나 이후인 경우
            if (product.getEndDate() != null && product.getEndDate().compareTo(now) >= 0) {
                product.setProductStatus(1); // productStatus를 1로 설정
            } else {
                product.setProductStatus(0); // 그렇지 않으면 0으로 설정
            }
        });
        productRepository.saveAll(products); // 변경된 상태를 저장
    }
}
