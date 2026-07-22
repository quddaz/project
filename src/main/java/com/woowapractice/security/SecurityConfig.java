package com.woowapractice.security;

import com.woowapractice.user.GitHubOAuth2UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "security.enabled", havingValue = "true")
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, OAuth2UserService<OAuth2UserRequest, OAuth2User> userService)
      throws Exception {
    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/",
                        "/api/problems",
                        "/api/problems/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/oauth2/**",
                        "/login/**",
                        "/css/**",
                        "/js/**")
                    .permitAll()
                    .requestMatchers("/api/admin/**", "/admin/**")
                    .authenticated()
                    .requestMatchers("/api/problems/*/submissions", "/api/submissions/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
        .oauth2Login(oauth -> oauth.userInfoEndpoint(userInfo -> userInfo.userService(userService)))
        .logout(logout -> logout.logoutSuccessUrl("/"));
    return http.build();
  }

  @Bean
  OAuth2UserService<OAuth2UserRequest, OAuth2User> githubOAuth2UserService(
      GitHubOAuth2UserService userService) {
    return userService;
  }
}
