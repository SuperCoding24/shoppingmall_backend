package com.github.shopping_mall_be.repository.User;

import com.github.shopping_mall_be.domain.UserEntity;
import com.github.shopping_mall_be.dto.User.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    UserDto save(UserDto userDto);
    public List<UserDto> findAll();
    UserDto findByEmail(String email);

    Optional<UserEntity> findByEmail2(String email);

    public void deleteByEmail(String email);


}