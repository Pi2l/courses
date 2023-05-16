package org.m.courses.api.v1.controller.user;

import io.swagger.v3.oas.annotations.media.Schema;
import org.m.courses.api.v1.controller.common.AbstractRequest;
import org.m.courses.model.Group;
import org.m.courses.model.Role;
import org.m.courses.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserRequest extends AbstractRequest<User> {

    @Schema(description = "First name", example = "firstname1")
    @NotBlank
    private String firstName;

    @Schema(description = "Last name", example = "lastname1")
    @NotBlank
    private String lastName;

    @Schema(description = "phone number", example = "0495434553", maxLength = 20)
    @NotBlank
    @Size(max = 20)
    private String phoneNumber;

    @Schema(description = "login", example = "user")
    @NotBlank
    private String login;

    @Schema(description = "password", example = "user")
    @NotBlank
    private String password;

    @Schema(description = "role", example = "USER")
    @NotNull
    private Role role;

    @Schema(description = "group id", example = "1", nullable = true)
    private Long groupId;

    public UserRequest() {
    }

    public UserRequest(User user) {
        this.setFirstName( user.getFirstName() );
        this.setLastName( user.getLastName() );
        this.setPhoneNumber( user.getPhoneNumber() );
        this.setLogin( user.getLogin() );
        this.setPassword( user.getPassword() );
        this.setRole( user.getRole() );

        Group group = user.getGroup();
        this.setGroupId( group == null ? null : group.getId() );
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

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
