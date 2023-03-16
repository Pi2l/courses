package org.m.courses.builder;

import org.m.courses.dao.UserDao;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;


@Component
public class UserBuilder {

    protected final UserDao userDao;

    protected User user;

    public static UserBuilder builder(UserDao userDao) {
        return new UserBuilder(userDao);
    }

    public UserBuilder(UserDao userDao) {
        user = new User();
        this.userDao = userDao;
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

    public User toDB() {
        return userDao.create( buildNew() );
    }

    private UserBuilder buildDefaultUser() {
        int randomValue = new SecureRandom().nextInt(100000);

        setId((long) randomValue);
        setFirstName("FirstName_" + randomValue);
        setLastName("LastName_" + randomValue);
        setPhoneNumber("PhoneNumber_" + randomValue);
        setLogin("Login_" + randomValue);
        setPassword("Password_" + randomValue);

        Role[] roles = Role.values();
        setRole( roles[Math.floorMod( randomValue, roles.length )] );
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
