package org.m.courses.api.v1.controller.users;

import org.m.courses.api.v1.controller.common.AbstractResponse;
import org.m.courses.model.User;

public class UserResponse extends AbstractResponse {

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String login;

    private String role;

    public UserResponse(User user) {
        super( user.getId() );
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.phoneNumber = user.getPhoneNumber();
        this.login = user.getLogin();
        this.role = user.getRole().name();
    }
}
