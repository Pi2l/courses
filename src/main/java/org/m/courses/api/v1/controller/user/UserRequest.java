package org.m.courses.api.v1.controller.user;

import org.m.courses.api.v1.controller.common.AbstractRequest;
import org.m.courses.model.Role;
import org.m.courses.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserRequest extends AbstractRequest<User> {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Size(max = 20)
    private String phoneNumber;

    @NotBlank
    private String login;

    @NotBlank
    private String password;

    @NotNull
    private Role role;

    public UserRequest() {
    }

    public UserRequest(User user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.phoneNumber = user.getPhoneNumber();
        this.login = user.getLogin();
        this.password = user.getPassword();
        this.role = user.getRole();
    }

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
