package com.github.shopping_mall_be.repository.Jwt;

import com.github.shopping_mall_be.dto.Jwt.Token;

public interface TokenRepository {

    Token save(Token token);
    Token findByRefreshToken(String refreshToken);
    Token findByUserEmail(String userEmail);
    Token findByAccessToken(String accessToken);
    void deleteById(Long id);

}