package com.github.shopping_mall_be.repository.User;


import com.github.shopping_mall_be.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
    Jpa를 이용한 Repository
*/

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    //email을 통해 사용자 정보를 가져옴
    UserEntity findByEmail(String email);
    Optional<UserEntity> findById(Long userId);
    UserEntity findByUserId(Long userId);

}