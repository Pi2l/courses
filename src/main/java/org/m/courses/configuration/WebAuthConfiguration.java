package org.m.courses.configuration;


import org.m.courses.model.Role;
import org.m.courses.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebAuthConfiguration {

    private UserDetailsServiceImpl userDetailsService;

    public WebAuthConfiguration(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .httpBasic()
                .and()
                .authorizeRequests()
                .antMatchers("/**").hasAnyRole(Role.ADMIN.name(), Role.TEACHER.name(), Role.USER.name())
                .and()
                .userDetailsService(userDetailsService)
        ;

        return http.build();
    }

}
