package com.github.shopping_mall_be.repository.User;

import com.github.shopping_mall_be.domain.UserEntity;
import com.github.shopping_mall_be.dto.User.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository //db 연동을 처리하는 DAO 클래스
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository{

    private final UserJpaRepository userJpaRepository;

    //회원 저장
    @Override
    public UserDto save(UserDto userDto) {
        return userJpaRepository.save(UserEntity.from(userDto)).toDTO();
    }

    //회원 전체 조회
    @Override
    public List<UserDto> findAll() {
        List<UserEntity> memberEntities = userJpaRepository.findAll();

        return memberEntities.stream()
                .map(UserEntity::toDTO)
                .collect(Collectors.toList());
    }

    //회원 이메일 조회
    @Override
    public UserDto findByEmail(String email) {
        UserEntity userDto = userJpaRepository.findByEmail(email);
        if (userDto == null) {
            return null;
        } else {
            return userDto.toDTO();
        }
    }

    @Override
    public Optional<UserEntity> findByEmail2(String email) {
        return Optional.ofNullable(userJpaRepository.findByEmail(email));
    }

    @Override
    public void deleteByEmail(String email) {
        UserEntity userEntity = userJpaRepository.findByEmail(email);
        if (userEntity != null) {
            userJpaRepository.delete(userEntity);
        } else {
            throw new IllegalArgumentException("User not found with email: " + email);
        }
    }




}