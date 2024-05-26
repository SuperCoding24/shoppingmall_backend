package com.github.shopping_mall_be.dto.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/*
    로그인 및 회원가입에 사용되는 데이터
*/

@Data
@AllArgsConstructor //모든 필드 값을 파라미터로 받는 생성자를 생성
@Builder
public class UserDto {

    private String email;
    private String user_password;
    private String user_nickname;
    private String user_phone;
    private String user_addr;
    private String user_img;

    @Builder.Default
    private final Role role = Role.USER;

    @Builder.Default
    private final Deleted deleted = Deleted.INUSE;

}