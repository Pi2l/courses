package org.m.courses.api.v1.controller.group;

import io.swagger.v3.oas.annotations.media.Schema;
import org.m.courses.api.v1.controller.common.AbstractResponse;
import org.m.courses.api.v1.controller.course.CourseResponse;
import org.m.courses.api.v1.controller.user.UserResponse;
import org.m.courses.model.Course;
import org.m.courses.model.Group;
import org.m.courses.model.User;

import java.util.Set;
import java.util.stream.Collectors;

public class GroupResponse extends AbstractResponse {

    @Schema(description = "Name", example = "name1")
    private String name;
    private Set<UserResponse> users;
    private Set<CourseResponse> courses;

    public GroupResponse(Group entity) {
        super( entity.getId() );
        this.name = entity.getName();

        Set<User> userSet = entity.getUsers();
        if (userSet != null) {
            this.users = userSet.stream()
                    .map(UserResponse::new).collect(Collectors.toSet());
        }

        Set<Course> courseSet = entity.getCourses();
        if (courseSet != null) {
            this.courses = courseSet.stream()
                    .map(CourseResponse::new).collect(Collectors.toSet());
        }
    }

    public String getName() {
        return name;
    }

    public Set<UserResponse> getUsers() {
        return users;
    }

    public Set<CourseResponse> getCourses() {
        return courses;
    }
}
