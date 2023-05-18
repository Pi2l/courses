package org.m.courses.service;

import org.m.courses.exception.UserUnauthenticatedException;
import org.m.courses.model.Role;
import org.m.courses.security.SpringUser;
import org.m.courses.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class UserAuthorizationService {

    public boolean isAdmin() {
        return hasCurrentUserRole(Role.ADMIN);
    }

    public boolean isTeacher() {
        return hasCurrentUserRole(Role.TEACHER);
    }

    public boolean isUser() {
        return hasCurrentUserRole(Role.USER);
    }

    private boolean hasCurrentUserRole(Role role) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return currentUser.getRole().equals( role );
        }
        return false;
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if ( authentication != null && authentication.isAuthenticated() ) {
            return hasAnySpringUserRole(authentication);
        }

        return false;
    }

    private boolean hasAnySpringUserRole(Authentication authentication) {
        for (Role role : Role.values()) {
            long springUserRolesCount = authentication.getAuthorities()
                    .stream().filter(r -> r.getAuthority().equals("ROLE_" + role.name())).count();

            if (springUserRolesCount > 0) {
                return true;
            }
        }
        return false;
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
