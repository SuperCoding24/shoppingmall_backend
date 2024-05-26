package com.github.shopping_mall_be.dto.Jwt;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/*
    에러 형식을 정의한 enum 클래스 --> 정의한 Exception 관리, 재사용 가능
    : 응답으로 보낼 HttpStatus(상태)와 에러메세지로 사용할 String 값을 가짐.
*/

@Getter
public enum ErrorMsg {

    UNKNOWN_ERROR(HttpStatus.UNAUTHORIZED,"인증 토큰이 존재하지 않습니다."),
    WRONG_TYPE_TOKEN(HttpStatus.UNAUTHORIZED,"잘못된 토큰 정보입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED,"만료된 토큰 정보입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED,"지원하지 않는 토큰 방식입니다."),
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED,"요청이 거절되었습니다."),
    AUTHORIZE_DENIED(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다.");

    Integer status;
    String messages;

    ErrorMsg(HttpStatus httpStatus, String s) {
        status = httpStatus.value();
        this.messages = s;
    }

    public String getMsg(){
        return this.messages;
    }

}