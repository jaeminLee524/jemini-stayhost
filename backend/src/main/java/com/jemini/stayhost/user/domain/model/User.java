package com.jemini.stayhost.user.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import com.jemini.stayhost.common.exception.AuthenticationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    public static User create(
        final String email,
        final String encodedPassword,
        final String name,
        final String phone
    ) {
        final User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.name = name;
        user.phone = stripHyphen(phone);
        user.status = UserStatus.ACTIVE;
        return user;
    }

    private static String stripHyphen(final String value) {
        return value != null ? value.replace("-", "") : null;
    }

    public void validateActive() {
        if (this.status != UserStatus.ACTIVE) {
            throw new AuthenticationException("비활성 상태의 계정입니다.");
        }
    }
}
