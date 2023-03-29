package org.m.courses.api.v1.controller.users;

import org.m.courses.api.v1.controller.common.AbstractRequest;
import org.m.courses.model.Role;
import org.m.courses.model.User;

public class UserRequest extends AbstractRequest<User> {

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String login;

    private String password;

    private Role role;

    @Override
    public User createEntity() {
        User user = new User();
        return updateEntity(user);
    }

    @Override
    public User updateEntity(User user) {
        user.setFirstName( firstName );
        user.setLastName( lastName );
        user.setPhoneNumber( phoneNumber );
        user.setLogin( login );
        user.setPassword( password );
        user.setRole( role );
        return user;
    }
}
