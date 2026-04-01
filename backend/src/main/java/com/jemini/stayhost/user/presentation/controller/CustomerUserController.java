package com.jemini.stayhost.user.presentation.controller;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.security.UserId;
import com.jemini.stayhost.user.application.dto.UserResult;
import com.jemini.stayhost.user.application.service.UserService;
import com.jemini.stayhost.user.presentation.docs.CustomerUserDocs;
import com.jemini.stayhost.user.presentation.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class CustomerUserController implements CustomerUserDocs {

    private final UserService userService;

    @GetMapping("/me")
    public ApiBaseResponse<UserResponse> getMe(
        final UserId userId
    ) {
        final UserResult result = userService.getMe(userId.value());

        return ApiBaseResponse.success(UserResponse.from(result));
    }
}
