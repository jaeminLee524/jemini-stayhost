package com.jemini.stayhost.common.presentation;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

  @GetMapping("/api/public/health")
  public ApiBaseResponse<String> health() {
    return ApiBaseResponse.success("OK");
  }
}
