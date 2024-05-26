package com.github.shopping_mall_be.domain;

import com.github.shopping_mall_be.dto.User.Deleted;
import com.github.shopping_mall_be.dto.User.Role;
import com.github.shopping_mall_be.dto.User.UserDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity //선언된 클래스를 DB 테이블과 매핑함
@Table(name = "users")  //Entity와 매핑할 테이블을 지정함
public class UserEntity implements Serializable {

    @Id //속성을 기본키로 설정
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "email") //객체 필드를 테이블의 칼럼과 매핑함
    private String email;

    @Column(name = "user_password")
    private String user_password;

    @Column(name = "user_nickname", nullable = false)
    private String user_nickname;

    @Column(name = "user_phone", nullable = false)
    private String user_phone;

    @Column(name = "user_addr", nullable = false)
    private String user_addr;

    @Column(name = "user_img", nullable = false)
    private String user_img;

    @Column(name = "user_role")
    @Enumerated(EnumType.STRING)
    private Role user_role;

    @Column(name = "created_at")
    @CreationTimestamp  //INSERT 할 때 자동으로 값을 채워줌
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    @UpdateTimestamp    //UPDATE 쿼리 발생시 해당 시간 값으로 쿼리 생성
    private LocalDateTime updated_at;

    @Column(name = "user_is_deleted")
    @Enumerated(EnumType.STRING)
    private Deleted deleted;

    public static UserEntity from(UserDto userDto){
        return UserEntity.builder()
                .email(userDto.getEmail())
                .user_password(userDto.getUser_password())
                .user_nickname(userDto.getUser_nickname())
                .user_phone(userDto.getUser_phone())
                .user_addr(userDto.getUser_addr())
                .user_img(userDto.getUser_img())
                .user_role(userDto.getRole())
                .created_at(LocalDateTime.now())
                .updated_at(LocalDateTime.now())
                .deleted(userDto.getDeleted())
                .build();
    }

    public UserDto toDTO(){
        return UserDto.builder()
                .email(this.email)
                .user_password(this.user_password)
                .user_nickname(this.user_nickname)
                .user_phone(this.user_phone)
                .user_addr(this.user_addr)
                .user_img(this.user_img)
                .build();
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderedItem> orderedItems;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;
}