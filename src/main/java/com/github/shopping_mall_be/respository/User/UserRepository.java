package com.github.shopping_mall_be.respository.User;

import com.github.shopping_mall_be.dto.User.UserDto;

import java.util.List;

public interface UserRepository {

    UserDto save(UserDto userDto);
    public List<UserDto> findAll();
    UserDto findByEmail(String email);
    public void deleteByEmail(String email);
}
