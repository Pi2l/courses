package org.m.courses.service;

import org.m.courses.exception.UserUnauthenticatedException;
import org.m.courses.model.Role;
import org.m.courses.model.SpringUser;
import org.m.courses.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserAuthorizationService {

    public boolean isAdmin() {
        return getRoles().contains( Role.ADMIN.name() );
    }

    public boolean isTeacher() {
        return getRoles().contains( Role.TEACHER.name() );
    }

    public boolean isUser() {
        return getRoles().contains( Role.USER.name() );
    }

    public boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null;
    }

    private List<String> getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UserUnauthenticatedException();
        }
        Collection<? extends GrantedAuthority> roles = authentication.getAuthorities();

        return roles.stream()
                .map( role -> role.getAuthority().substring("ROLE_".length()) )
                .collect(Collectors.toList());
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UserUnauthenticatedException();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof SpringUser) {
            return ((SpringUser) principal ).getUser();
        }
        return null;
    }
}
