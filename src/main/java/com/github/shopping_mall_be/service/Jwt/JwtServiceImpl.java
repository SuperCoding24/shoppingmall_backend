package com.github.shopping_mall_be.service.Jwt;

import com.github.shopping_mall_be.dto.Jwt.JwtProvider;
import com.github.shopping_mall_be.dto.Jwt.Token;
import com.github.shopping_mall_be.dto.User.Role;
import com.github.shopping_mall_be.dto.User.UserDto;
import com.github.shopping_mall_be.repository.Jwt.TokenRepository;
import com.github.shopping_mall_be.repository.User.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j  //log.info 사용 가능
@Service
@RequiredArgsConstructor
@Transactional
public class JwtServiceImpl implements JwtService{

    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    public void createAccessTokenHeader(HttpServletResponse response, String refreshToken) {
        if(jwtProvider.validateToken(refreshToken)) {
            Token byRefreshToken = tokenRepository.findByRefreshToken(refreshToken);

            String userEmail = byRefreshToken.getUserEmail();
            log.info("userEmail : " + userEmail);

            UserDto byUserEmail = userRepository.findByEmail(userEmail);
            log.info("member : " + byUserEmail);

            List<GrantedAuthority> authorities = getAuthoritiesForUser(byUserEmail);

            Token newToken = jwtProvider.reIssueAccessToken(userEmail, authorities);
            byRefreshToken.setAccessToken(newToken.getAccessToken(), newToken.getAccessTokenTime());

            HttpHeaders headers = new HttpHeaders();

            //refreshToken 만료전 재발행
            if(byRefreshToken.getRefreshTokenTime().isBefore(LocalDateTime.now().minusDays(1))){
                long now = (new Date()).getTime();
                Date refreshTokenExpire = new Date(System.currentTimeMillis() + 1000 * 60 * 60);
                refreshToken = jwtProvider.reIssueRefreshToken(refreshTokenExpire);

                byRefreshToken.setRefreshToken(refreshToken, refreshTokenExpire.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                tokenRepository.save(byRefreshToken);
                jwtProvider.sendAccessAndRefreshToken(response, byRefreshToken.getAccessToken(), byRefreshToken.getRefreshToken());
            }
            else{
                tokenRepository.save(byRefreshToken);
                jwtProvider.sendAccessToken(response, byRefreshToken.getAccessToken());
            }

        } else {
            throw new IllegalArgumentException("Unexpected token");
        }
    }

    @Override
    public UserDto checkAccessTokenValid(String accessToken) {
        if(jwtProvider.validateToken(accessToken)) {
            Token byAccessToken = tokenRepository.findByAccessToken(accessToken);

            String userEmail = byAccessToken.getUserEmail();
            log.info("userEmail : " + userEmail);

            UserDto byUserEmail = userRepository.findByEmail(userEmail);
            log.info("member : " + byUserEmail);

            return byUserEmail;
        }
        else{
            return null;
        }
    }

    private List<GrantedAuthority> getAuthoritiesForUser(UserDto byUserEmail) {

        Role role = byUserEmail.getRole();

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" +role.name()));
        log.info("role : " + role.name());
        log.info("authorities : " + authorities);
        return authorities;

    }

}