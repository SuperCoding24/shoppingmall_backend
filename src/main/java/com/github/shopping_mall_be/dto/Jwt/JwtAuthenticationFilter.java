package com.github.shopping_mall_be.dto.Jwt;

import com.github.shopping_mall_be.dto.User.UserDto;
import com.github.shopping_mall_be.service.Jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
OncePerRequestFilter -->
    : 동일한 요청에 대해서 한번만 Filtering 할 수 있게 해준다. (요청당 한번의 실행을 보장)
    인증 또는 인가를 거치고 특정 url로 요청하면 인증 및 인가필터를 다시 실행시켜야 하지만,
    OncePerRequestFilter를 사용하여 인증이나 인가를 한번만 거치고 다음 로직을 진행할 수 있도록 한다.
*/

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String[] whitelist = {"/api/signup", "/api/logout","/api/login", "/swagger-ui/**", "/swagger-ui/index.html","/error","/v3/**"};
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(checkPathFree(request.getRequestURI())){
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = jwtProvider.extractRefreshToken(request)
                .filter(jwtProvider::validateToken)
                .orElse(null);

        //refreshToken이 담아져서 오는 경우 accesstoken재발급
        if (refreshToken != null) {
            jwtService.createAccessTokenHeader(response, refreshToken);
            return;
        }
        else{
            //refreshToken이 없다면 accessToken을 재발급
            checkAccessTokenAndAuthentication(request, response, filterChain);
        }

    }

    public boolean checkPathFree(String requestURI){
        return PatternMatchUtils.simpleMatch(whitelist, requestURI);
    }

    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  FilterChain filterChain) throws ServletException, IOException {
        log.info("checkAccessTokenAndAuthentication() 호출");
        String accessToken = jwtProvider.extractAccessToken(request)
                .filter(jwtProvider::validateToken).orElse(null);

        if (accessToken == null) {
            log.info("유효한 AccessToken이 없습니다.");
        } else {
            log.info("AccessToken 검증 성공: {}", accessToken);
        }

        UserDto userDto = jwtService.checkAccessTokenValid(accessToken);
        if(userDto != null){
            log.info("사용자 인증 성공: {}", userDto.getEmail());
            this.saveAuthentication(userDto);
        }else{
            log.info("사용자 인증 실패");
        }

        filterChain.doFilter(request, response);
    }

    public void saveAuthentication(UserDto userDto) {
        String password = userDto.getUser_password();

        UserDetails userDetailsUser = org.springframework.security.core.userdetails.User.builder()
                .username(userDto.getEmail())
                .password(password)
                .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetailsUser, null,
                        authoritiesMapper.mapAuthorities(userDetailsUser.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("SecurityContext에 인증 정보 저장됨: {}", authentication.getName());

    }




}