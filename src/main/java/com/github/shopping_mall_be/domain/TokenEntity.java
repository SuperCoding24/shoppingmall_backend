package com.github.shopping_mall_be.domain;

import com.github.shopping_mall_be.dto.Jwt.Token;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity (name = "token")
public class TokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //기본키를 db가 자동으로 생성해줌
    private Long id;    //토큰 id

    private String grantType;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime accessTokenTime;
    private LocalDateTime refreshTokenTime;

    private String userEmail;

    public static TokenEntity from(Token token) {
        return TokenEntity.builder()
                .id(token.getId())
                .grantType(token.getGrantType())
                .accessToken(token.getAccessToken())
                .accessTokenTime(token.getAccessTokenTime())
                .refreshToken(token.getRefreshToken())
                .refreshTokenTime(token.getRefreshTokenTime())
                .userEmail(token.getUserEmail())
                .build();
    }

}