package com.github.muhammadabdurrahman.librarymanagementsystem.configuration.ratelimiter;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RateLimiterFilterConfig {

  private final RateLimiterFilter rateLimiterFilter;

  @Bean
  public FilterRegistrationBean<RateLimiterFilter> loggingFilter() {
    FilterRegistrationBean<RateLimiterFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(rateLimiterFilter);
    registrationBean.addUrlPatterns("/api/*");
    return registrationBean;
  }
}