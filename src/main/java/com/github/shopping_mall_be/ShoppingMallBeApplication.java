package com.github.shopping_mall_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShoppingMallBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingMallBeApplication.class, args);
    }

}
