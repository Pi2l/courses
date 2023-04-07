package org.m.courses.dao;

import org.junit.jupiter.api.BeforeEach;
import org.m.courses.auth.AuthManager;
import org.m.courses.builder.UserBuilder;
import org.m.courses.model.Role;
import org.springframework.beans.factory.annotation.Autowired;

public class Autologinable {

    @Autowired
    private UserBuilder userBuilder;

    @BeforeEach
    void login() {
        AuthManager.loginAs( userBuilder.setRole(Role.ADMIN).build() );
    }
}
