package com.github.shopping_mall_be.service.User;

import com.github.shopping_mall_be.controller.UserResponse;
import com.github.shopping_mall_be.dto.Jwt.Token;
import com.github.shopping_mall_be.dto.User.NewUserDto;
import com.github.shopping_mall_be.dto.User.UserDto;
import com.github.shopping_mall_be.dto.User.getUserDto;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

    String encodePassword(String password);
    boolean matchesPassword(String rawPassword, String encodedPassword);

    UserDto register(NewUserDto userDto);
    void unregister(String email);

    Token login(String email, String pw) throws Exception;
    String logout(HttpServletRequest request, String email);

    UserResponse getByEmail(String email);
    getUserDto getUserById(Long id);

}