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
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getLogin(), user.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())) );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return user;
    }

    public static void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public static SpringUser getSpringUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return (SpringUser) authentication.getPrincipal();
    }
}
