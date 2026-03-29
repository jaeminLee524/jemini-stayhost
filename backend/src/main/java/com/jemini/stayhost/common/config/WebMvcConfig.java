package com.jemini.stayhost.common.config;

import com.jemini.stayhost.common.security.PartnerIdResolver;
import com.jemini.stayhost.common.security.UserIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserIdResolver userIdResolver;
    private final PartnerIdResolver partnerIdResolver;

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userIdResolver);
        resolvers.add(partnerIdResolver);
    }
}
