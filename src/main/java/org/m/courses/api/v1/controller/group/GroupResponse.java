package org.m.courses.api.v1.controller.group;

import org.m.courses.api.v1.controller.common.AbstractResponse;
import org.m.courses.api.v1.controller.user.UserResponse;
import org.m.courses.model.Group;
import org.m.courses.model.User;

import java.util.Set;
import java.util.stream.Collectors;

public class GroupResponse extends AbstractResponse {

    private String name;
    private Set<UserResponse> users;

    public GroupResponse(Group entity) {
        super( entity.getId() );
        this.name = entity.getName();

        Set<User> userSet = entity.getUsers();
        if (userSet != null) {
            this.users = userSet.stream()
                    .map(UserResponse::new).collect(Collectors.toSet());
        }
    }
}
