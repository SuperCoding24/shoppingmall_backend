package com.github.shopping_mall_be.config;

import com.github.shopping_mall_be.dto.Jwt.JwtAccessDeniedHandler;
import com.github.shopping_mall_be.dto.Jwt.JwtAuthenticationFilter;
import com.github.shopping_mall_be.dto.Jwt.JwtExceptionFilter;
import com.github.shopping_mall_be.dto.Jwt.JwtProvider;
import com.github.shopping_mall_be.service.Jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)

public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JwtService jwtService;
    private final JwtExceptionFilter jwtExceptionFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable) //기본 로그인 방식 비활성화 --> jwt사용
                .csrf(AbstractHttpConfigurer::disable)      //csrf 보안 비활성화 --> jwt사용
                .formLogin(AbstractHttpConfigurer::disable) //Security에서 제공하는 기본 폼 로그인 방식 비활성화
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(se -> se
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));  //session 사용 하지 않음

        http    //특정 URL에 대한 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api").permitAll()    //모든 권한 허용
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/api/signup").permitAll()
                        .requestMatchers("/unregister").permitAll()
                        .requestMatchers("/unregister/**").permitAll()
                        .requestMatchers("/unregister/{user_email}").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/v3/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider, jwtService), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter,JwtAuthenticationFilter.class);

        http
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(new JwtAccessDeniedHandler())
                );

        return http.build();

    }

    @Bean
    PasswordEncoder passwordEncoder() {
        String idForEncode = "bcrypt";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(idForEncode, new BCryptPasswordEncoder());
        return new DelegatingPasswordEncoder(idForEncode, encoders);
    }

}