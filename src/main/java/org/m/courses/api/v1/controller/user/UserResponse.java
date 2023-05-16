package org.m.courses.api.v1.controller.user;

import io.swagger.v3.oas.annotations.media.Schema;
import org.m.courses.api.v1.controller.common.AbstractResponse;
import org.m.courses.api.v1.controller.group.GroupResponse;
import org.m.courses.model.Group;
import org.m.courses.model.User;

public class UserResponse extends AbstractResponse {

    @Schema(description = "First name", example = "firstname1")
    private String firstName;

    @Schema(description = "Last name", example = "lastname1")
    private String lastName;

    @Schema(description = "phone number", example = "0495434553")
    private String phoneNumber;

    @Schema(description = "login", example = "user")
    private String login;

    @Schema(description = "role", example = "USER")
    private String role;

    private GroupResponse group;

    public UserResponse(User user) {
        super( user.getId() );
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.phoneNumber = user.getPhoneNumber();
        this.login = user.getLogin();
        this.role = user.getRole().name();

        Group group = user.getGroup();
        if (group != null) {
            this.group = new GroupResponse( group );
        }
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public GroupResponse getGroup() {
        return group;
    }

    public void setGroup(GroupResponse group) {
        this.group = group;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
