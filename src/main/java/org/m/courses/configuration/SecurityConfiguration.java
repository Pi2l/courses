package org.m.courses.configuration;


import org.m.courses.security.UserDetailsServiceImpl;
import org.m.courses.security.jwt.JwtAuthenticationFilter;
import org.m.courses.security.jwt.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;

import javax.servlet.Filter;

import static org.m.courses.api.v1.controller.common.ApiPath.API;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String EXCLUDED_PATH = "/api/login";
    private static final String AUTH_REQUIRED_PATH = "/**";
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfiguration(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(API + "/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterAfter( getJwtAuthenticationFilter( jwtService ), ExceptionTranslationFilter.class)
                .userDetailsService(userDetailsService)
        ;

        return http.build();
    }

    private Filter getJwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(AUTH_REQUIRED_PATH, EXCLUDED_PATH, jwtService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder getPasswordEncoder() {
      return new BCryptPasswordEncoder();
    }

}
