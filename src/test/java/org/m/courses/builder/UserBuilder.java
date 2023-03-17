package org.m.courses.builder;

import org.m.courses.dao.UserDao;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;


@Component
public class UserBuilder {

    @Autowired private UserDao userDao;

    private User user;

    {
        buildDefaultUser();
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public User build() {
        User odlUser = user;
        buildDefaultUser();
        return odlUser;
    }

    public User buildNew() {
        return setId(null)
                .build();
    }

    // Spring based
    public User toDB() {
        return userDao.create( buildNew() ).get();
    }

    private UserBuilder buildDefaultUser() {
        long randomValue = Math.abs(new SecureRandom().nextLong()) % 10000;
        this.user = new User();

        setId(randomValue);
        setFirstName("FirstName_" + randomValue);
        setLastName("LastName_" + randomValue);
        setPhoneNumber("PhoneNumber_" + randomValue);
        setLogin("Login_" + randomValue);
        setPassword("Password_" + randomValue);
        setRole( Role.USER );
        return this;
    }

    public UserBuilder setId(Long id) {
        user.setId(id);
        return this;
    }

    public UserBuilder setFirstName(String str) {
        user.setFirstName(str);
        return this;
    }

    public UserBuilder setLastName(String str) {
        user.setLastName(str);
        return this;
    }

    public UserBuilder setPhoneNumber(String str) {
        user.setPhoneNumber(str);
        return this;
    }

    public UserBuilder setLogin(String str) {
        user.setLogin(str);
        return this;
    }

    public UserBuilder setPassword(String str) {
        user.setPassword(str);
        return this;
    }

    public UserBuilder setRole(Role role) {
        user.setRole(role);
        return this;
    }
}
