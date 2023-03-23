package org.m.courses.auth;

import org.m.courses.model.SpringUser;
import org.m.courses.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.List;


public class AuthManager {

    public static User loginAs(User user) {
        SecurityContextHolder.clearContext();

        SpringUser springUser = new SpringUser( user );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                springUser, springUser.getUser().getPassword(), List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())) );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return user;
    }
}
