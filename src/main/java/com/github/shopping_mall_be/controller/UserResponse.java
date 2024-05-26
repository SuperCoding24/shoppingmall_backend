package com.github.shopping_mall_be.controller;

import com.github.shopping_mall_be.dto.User.UserDto;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserResponse {

    private final String email;

    @Builder
    public UserResponse(String email) {
        this.email = email;
    }

    public static UserResponse of(UserDto userDto){
        return UserResponse.builder()
                .email(userDto.getEmail())
                .build();
    }

}