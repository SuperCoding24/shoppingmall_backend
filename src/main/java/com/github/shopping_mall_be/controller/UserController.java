package com.github.shopping_mall_be.controller;

import com.github.shopping_mall_be.dto.Jwt.ResponseToken;
import com.github.shopping_mall_be.dto.Jwt.Token;
import com.github.shopping_mall_be.dto.User.NewUserDto;
import com.github.shopping_mall_be.dto.User.UserDto;
import com.github.shopping_mall_be.dto.User.getUserDto;
import com.github.shopping_mall_be.service.User.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
@Tag(name = "User API", description = "User API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "회원가입을 위한 엔드포인트입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> join(@Validated @RequestBody NewUserDto userDto, BindingResult result) {
        try {
            if (!isValidEmail(userDto.getEmail())) {
                result.rejectValue("email", "email.invalid", "이메일 형식에 맞게 입력하세요.");
                log.info("이메일 형식에 맞게 입력하세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일 형식에 맞게 입력하세요.");
            }

            if (!isValidPassword(userDto.getUser_password())) {
                result.rejectValue("password", "password.invalid", "비밀번호 형식에 맞게 입력해주세요.");
                log.info("비밀번호 형식에 맞게 입력해주세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호 형식에 맞게 입력해주세요.");
            }

            if (!isValidPhone(userDto.getUser_phone())) {
                result.rejectValue("user_phone", "user_phone.invalid", "핸드폰 입력 형식에 맞게 입력해주세요.");
                log.info("핸드폰 입력 형식에 맞게 입력해주세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("핸드폰 입력 형식에 맞게 입력해주세요.");
            }

            UserDto saveUser = userService.register(userDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.of(saveUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }



    @Operation(summary = "로그인", description = "로그인을 위한 엔드포인트입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto login) {
        try {
            String email = login.getEmail();
            String password = login.getUser_password();
            Token token = userService.login(email, password);
            return ResponseEntity.ok().body(ResponseToken.of(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 위한 엔드포인트입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, @RequestBody UserDto userDto) {
        String res = userService.logout(request, userDto.getEmail());
        return ResponseEntity.ok().body(res);
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 위한 엔드포인트입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @DeleteMapping("/unregister/{email}")
    public ResponseEntity<String> unregister(@PathVariable String email) {
        userService.unregister(email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("회원 탈퇴 되었습니다.");
    }

    @Operation(summary = "회원 정보 조회", description = "회원 ID로 회원 정보를 조회하는 엔드포인트입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<getUserDto> getUserById(@PathVariable Long userId) {
        getUserDto getuserDto = userService.getUserById(userId);
        if (getuserDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(getuserDto);
    }

    //이메일 유효성 검사
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    //비밀번호 유효성 검사
    private boolean isValidPassword(String password) {
        String passwordRegex = "(?=.*[0-9])(?=.*[A-Za-z]).{8,20}$";
        return password.matches(passwordRegex);
    }

    //핸드폰 번호 유효성 검사
    private boolean isValidPhone (String user_phone) {
        String phoneRegex = "^01([0|1|6|7|8|9])-([0-9]{3,4})-([0-9]{4})+$";
        return user_phone.matches(phoneRegex);
    }
}