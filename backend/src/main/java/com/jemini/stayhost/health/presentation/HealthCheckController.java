package com.jemini.stayhost.health.presentation;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class HealthCheckController {

    @GetMapping("/health")
    public ApiBaseResponse<String> health() {
        return ApiBaseResponse.success("OK");
    }
}
