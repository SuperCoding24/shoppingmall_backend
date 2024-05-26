package com.github.shopping_mall_be.dto.Jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } catch (JwtException ex) {
            String message = ex.getMessage();
            if(ErrorMsg.UNKNOWN_ERROR.getMsg().equals(message)) {
                setResponse(response, ErrorMsg.UNKNOWN_ERROR);
            }
            //잘못된 타입의 토큰
            else if(ErrorMsg.WRONG_TYPE_TOKEN.getMsg().equals(message)) {
                setResponse(response, ErrorMsg.WRONG_TYPE_TOKEN);
            }
            //만료된 토큰
            else if(ErrorMsg.EXPIRED_TOKEN.getMsg().equals(message)) {
                setResponse(response, ErrorMsg.EXPIRED_TOKEN);
            }
            //지원되지 않는 토큰
            else if(ErrorMsg.UNSUPPORTED_TOKEN.getMsg().equals(message)) {
                setResponse(response, ErrorMsg.UNSUPPORTED_TOKEN);
            }
            else {
                setResponse(response, ErrorMsg.ACCESS_DENIED);
            }
        }
    }

    private void setResponse(HttpServletResponse response, ErrorMsg errorMessage) throws RuntimeException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorMessage.getStatus());
        response.getWriter().print(errorMessage.getMsg());
    }

}