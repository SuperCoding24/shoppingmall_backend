package com.github.shopping_mall_be.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;


@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new ResponseEntity<>("해당 id를 가진 물건은 없습니다.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
    }
}
