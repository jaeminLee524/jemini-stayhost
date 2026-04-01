package com.jemini.stayhost.user.presentation.controller;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.user.application.dto.UserLoginResult;
import com.jemini.stayhost.user.application.dto.UserResult;
import com.jemini.stayhost.user.application.service.UserService;
import com.jemini.stayhost.user.presentation.docs.CustomerAuthDocs;
import com.jemini.stayhost.user.presentation.dto.UserLoginRequest;
import com.jemini.stayhost.user.presentation.dto.UserLoginResponse;
import com.jemini.stayhost.user.presentation.dto.UserSignupRequest;
import com.jemini.stayhost.user.presentation.dto.UserSignupResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/users")
@RequiredArgsConstructor
public class CustomerAuthController implements CustomerAuthDocs {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiBaseResponse<UserSignupResponse> signup(
        @RequestBody @Valid final UserSignupRequest request
    ) {
        final UserResult result = userService.signup(request.toCommand());

        return ApiBaseResponse.success(UserSignupResponse.from(result));
    }

    @PostMapping("/login")
    public ApiBaseResponse<UserLoginResponse> login(
        @RequestBody @Valid final UserLoginRequest request
    ) {
        final UserLoginResult result = userService.login(request.toCommand());

        return ApiBaseResponse.success(UserLoginResponse.from(result));
    }
}
