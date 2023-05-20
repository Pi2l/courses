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

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String EXCLUDED_PATH = "/api/login";
    private static final String AUTH_REQUIRED_PATH = "/api/**";
    private static final String OPENAPI_PATHS[] = {
            "/swagger-ui/index.html",
            "/swagger-ui/swagger-ui.css",
            "/swagger-ui/index.css",
            "/swagger-ui/swagger-ui-standalone-preset.js",
            "/swagger-ui/swagger-ui-bundle.js",
            "/swagger-ui/swagger-initializer.js",
            "/swagger-ui/favicon-32x32.png",
            "/v3/api-docs/swagger-config",
            "/v3/api-docs" };
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfiguration(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(EXCLUDED_PATH).permitAll()
                .antMatchers(OPENAPI_PATHS).permitAll()
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
