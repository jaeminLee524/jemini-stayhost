package com.jemini.stayhost.user.application.service;

import com.jemini.stayhost.common.exception.AuthenticationException;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.security.JwtProvider;
import com.jemini.stayhost.user.application.dto.UserLoginCommand;
import com.jemini.stayhost.user.application.dto.UserLoginResult;
import com.jemini.stayhost.user.application.dto.UserResult;
import com.jemini.stayhost.user.application.dto.UserSignupCommand;
import com.jemini.stayhost.user.domain.component.UserManager;
import com.jemini.stayhost.user.domain.component.UserReader;
import com.jemini.stayhost.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String ROLE_GUEST = "GUEST";
    private static final String CONTEXT_CUSTOMER = "CUSTOMER";
    private static final long SECONDS_DIVISOR = 1000;

    private final UserReader userReader;
    private final UserManager userManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * 회원가입. 이메일 중복 검증 후 ACTIVE 상태로 생성한다.
     */
    @Transactional
    public UserResult signup(final UserSignupCommand command) {
        validateDuplicateEmail(command.email());

        final User user = createUser(command);
        final User saved = userManager.save(user);

        return UserResult.from(saved);
    }

    /**
     * 로그인. 비밀번호 검증 후 JWT를 발급한다.
     */
    @Transactional(readOnly = true)
    public UserLoginResult login(final UserLoginCommand command) {
        final User user = userReader.getByEmail(command.email());

        validatePassword(command.password(), user.getPassword());
        user.validateActive();

        return buildLoginResult(user);
    }

    /**
     * 내 정보 조회.
     */
    @Transactional(readOnly = true)
    public UserResult getMe(final Long userId) {
        final User user = userReader.getById(userId);

        return UserResult.from(user);
    }

    // -- signup private methods --

    private void validateDuplicateEmail(final String email) {
        if (userReader.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private User createUser(final UserSignupCommand command) {
        return User.create(
            command.email(),
            passwordEncoder.encode(command.password()),
            command.name(),
            command.phone()
        );
    }

    // -- login private methods --

    private void validatePassword(final String rawPassword, final String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new AuthenticationException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    private UserLoginResult buildLoginResult(final User user) {
        final String token = jwtProvider.generateToken(user.getId(), ROLE_GUEST, CONTEXT_CUSTOMER);

        return UserLoginResult.builder()
            .accessToken(token)
            .expiresIn(jwtProvider.getExpiration() / SECONDS_DIVISOR)
            .userId(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .build();
    }
}
