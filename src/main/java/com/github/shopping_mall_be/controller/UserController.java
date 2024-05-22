package com.github.shopping_mall_be.controller;

import com.github.shopping_mall_be.dto.Jwt.ResponseToken;
import com.github.shopping_mall_be.dto.Jwt.Token;
import com.github.shopping_mall_be.dto.User.NewUserDto;
import com.github.shopping_mall_be.dto.User.UserDto;
import com.github.shopping_mall_be.service.User.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api") //내부에 선언한 메서드의 URL 리소스 앞에 @RequestMapping의 값이 공통 값으로 추가됨.
@RequiredArgsConstructor
@RestController //사용자 요청을 제어하는 controller 클래스
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> join(@Validated @RequestBody NewUserDto userDto, BindingResult result) {
        try{

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
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto login) {
        try {
            String email = login.getEmail();
            String password = login.getUser_password();
            Token token = userService.login(email, password);
            ResponseEntity.ok().body(ResponseToken.of(token));

            return new ResponseEntity<>("로그인 되었습니다.", HttpStatus.CREATED);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, @RequestBody UserDto userDto) {
        String res = userService.logout(request, userDto.getEmail());
        return ResponseEntity.ok().body(res);
    }

    @DeleteMapping("/unregister/{email}")
    public ResponseEntity<String> unregister(@PathVariable String email) {
        userService.unregister(email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("회원 탈퇴 되었습니다.");
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
