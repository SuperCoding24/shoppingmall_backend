package com.github.shopping_mall_be.dto.Jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider {

    private static final String SUBJECT_ACCESS = "access_token";
    private static final String SUBJECT_REFRESH = "refresh_token";
    private static final String EMAIL_CLAIMS = "email";
    private static final String AUTHORITIES_KEY = "auth";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_AUTHORIZATION_Refresh = "Authorization-refresh";

    @Value("${jwt.secret_key}")
    private String secretKey;

    private Key key;
    private final ObjectMapper objectMapper;
    
    @PostConstruct
    public void Init() {

        byte[] keyBytes = DatatypeConverter.parseBase64Binary(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Token createToken(Authentication authentication, List<GrantedAuthority> authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(AUTHORITIES_KEY, authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        claims.put(EMAIL_CLAIMS, authentication.getName());

        long now = (new Date()).getTime();

        //AccessToken 생성 (30분)
        Date accessTokenExpire = new Date(System.currentTimeMillis() + 1000 * 60 * 10000);
        claims.put("exp", accessTokenExpire);
        String accessToken = createAccessToken(claims, accessTokenExpire);

        //RefreshToken 생성 (1시간)
        Date refreshTokenExpire = new Date(System.currentTimeMillis() + 1000 * 60 * 20000);
        claims.put("exp", refreshTokenExpire);
        String refreshToken = createRefreshToken(claims, refreshTokenExpire);

        Token token = Token.builder()
                .grantType("Bearer ")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenTime((accessTokenExpire.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .refreshTokenTime((refreshTokenExpire.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .userEmail(authentication.getName())
                .build();

        log.info("token in JwtProvider : " + token);
        return token;
    }

    public String createAccessToken(Map<String, Object> claims, Date expiredTime) {
        long now = new Date().getTime();
        return Jwts.builder()
                .setSubject(SUBJECT_ACCESS)
                .setClaims(claims)
                .setExpiration(expiredTime)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    public String createRefreshToken(Map<String, Object> claims, Date expiredTime) {
        long now = new Date().getTime();
        return Jwts.builder()
                .setSubject(SUBJECT_REFRESH)
                .setExpiration(expiredTime)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(HEADER_AUTHORIZATION, accessToken);
    }

    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(HEADER_AUTHORIZATION_Refresh, refreshToken);
    }

    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        response.setHeader(HEADER_AUTHORIZATION, accessToken);
        log.info("재발급된 AccessToken : {}", accessToken);
    }

    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
        log.info("Access Token, Refresh Token 헤더 설정 완료");
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HEADER_AUTHORIZATION_Refresh))
                .filter(refreshToken -> refreshToken.startsWith("Bearer "))
                .map(refreshToken -> refreshToken.replace("Bearer ", ""));
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HEADER_AUTHORIZATION))
                .filter(refreshToken -> refreshToken.startsWith("Bearer "))
                .map(refreshToken -> refreshToken.replace("Bearer ", ""));
    }

    public Token reIssueAccessToken(String userEmail, List<GrantedAuthority> authorities) {
        Long now = (new Date()).getTime();
        Date accessTokenExpire = new Date(System.currentTimeMillis() + 1000 * 60 * 30);

        log.info("authorities : " + authorities);

        Map<String, Object> claims = new HashMap<>();
        claims.put(AUTHORITIES_KEY, authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        claims.put(EMAIL_CLAIMS, userEmail);

        String accessToken = createAccessToken(claims, accessTokenExpire);

        Token token = Token.builder()
                .grantType("Bearer ")
                .accessToken(accessToken)
                .userEmail(userEmail)
                .accessTokenTime(accessTokenExpire.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .build();

        return token;
    }

    //refreshToken재발급
    public String reIssueRefreshToken(Date expiredTime){
        Map<String, Object> claims = new HashMap<>();
        claims.put("exp", expiredTime);

        return createRefreshToken(claims, expiredTime);
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return true;
        }
        catch (Exception e) {
            throw new JwtException("오류가 발생했습니다");
        }
    }

}