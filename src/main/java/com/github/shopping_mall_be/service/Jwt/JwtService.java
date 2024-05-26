package com.github.shopping_mall_be.service.Jwt;

import com.github.shopping_mall_be.dto.User.UserDto;
import jakarta.servlet.http.HttpServletResponse;

public interface JwtService {

    void createAccessTokenHeader(HttpServletResponse response, String refreshToken);
    UserDto checkAccessTokenValid(String accessToken);

}