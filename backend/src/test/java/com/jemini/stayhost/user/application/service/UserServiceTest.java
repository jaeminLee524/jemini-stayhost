package com.jemini.stayhost.user.application.service;

import com.jemini.stayhost.common.exception.AuthenticationException;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.exception.NotFoundException;
import com.jemini.stayhost.common.security.JwtProvider;
import com.jemini.stayhost.user.application.dto.UserLoginCommand;
import com.jemini.stayhost.user.application.dto.UserLoginResult;
import com.jemini.stayhost.user.application.dto.UserResult;
import com.jemini.stayhost.user.application.dto.UserSignupCommand;
import com.jemini.stayhost.user.domain.component.UserManager;
import com.jemini.stayhost.user.domain.component.UserReader;
import com.jemini.stayhost.user.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserReader userReader;

    @Mock
    private UserManager userManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("회원가입 성공")
    void 회원가입_성공() {
        final UserSignupCommand command = UserSignupCommand.builder()
            .email("user@test.com").password("password123").name("홍길동").phone("010-1234-5678")
            .build();
        given(userReader.existsByEmail("user@test.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encoded");
        given(userManager.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        final UserResult result = userService.signup(command);

        assertThat(result.email()).isEqualTo("user@test.com");
        assertThat(result.name()).isEqualTo("홍길동");
        verify(userManager).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 - 이메일 중복이면 예외")
    void 회원가입_이메일_중복이면_예외() {
        final UserSignupCommand command = UserSignupCommand.builder()
            .email("duplicate@test.com").build();
        given(userReader.existsByEmail("duplicate@test.com")).willReturn(true);

        assertThatThrownBy(() -> userService.signup(command))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("로그인 성공")
    void 로그인_성공() {
        final User user = createActiveUser();
        given(userReader.getByEmail("user@test.com")).willReturn(user);
        given(passwordEncoder.matches("password123", "encoded")).willReturn(true);
        given(jwtProvider.generateToken(any(), anyString(), anyString())).willReturn("jwt-token");
        given(jwtProvider.getExpiration()).willReturn(1800000L);

        final UserLoginResult result = userService.login(
            UserLoginCommand.builder().email("user@test.com").password("password123").build());

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.email()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("로그인 - 이메일이 존재하지 않으면 예외")
    void 로그인_이메일이_존재하지_않으면_예외() {
        given(userReader.getByEmail("unknown@test.com"))
            .willThrow(new NotFoundException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> userService.login(
            UserLoginCommand.builder().email("unknown@test.com").password("password123").build()))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("로그인 - 비밀번호가 틀리면 예외")
    void 로그인_비밀번호가_틀리면_예외() {
        final User user = createActiveUser();
        given(userReader.getByEmail("user@test.com")).willReturn(user);
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

        assertThatThrownBy(() -> userService.login(
            UserLoginCommand.builder().email("user@test.com").password("wrong").build()))
            .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void 내_정보_조회_성공() {
        final User user = createActiveUser();
        given(userReader.getById(1L)).willReturn(user);

        final UserResult result = userService.getMe(1L);

        assertThat(result.email()).isEqualTo("user@test.com");
        assertThat(result.name()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("내 정보 조회 - 사용자 없으면 예외")
    void 내_정보_조회_사용자_없으면_예외() {
        given(userReader.getById(999L))
            .willThrow(new NotFoundException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> userService.getMe(999L))
            .isInstanceOf(NotFoundException.class);
    }

    private User createActiveUser() {
        return User.create("user@test.com", "encoded", "홍길동", "010-1234-5678");
    }
}
