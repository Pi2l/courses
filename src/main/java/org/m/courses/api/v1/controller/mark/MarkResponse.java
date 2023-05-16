package org.m.courses.api.v1.controller.mark;

import io.swagger.v3.oas.annotations.media.Schema;
import org.m.courses.api.v1.controller.common.AbstractResponse;
import org.m.courses.api.v1.controller.course.CourseResponse;
import org.m.courses.api.v1.controller.group.GroupResponse;
import org.m.courses.api.v1.controller.user.UserResponse;
import org.m.courses.model.Mark;

public class MarkResponse extends AbstractResponse {

    private CourseResponse course;
    private UserResponse user;

    @Schema(description = "Mark value", minimum = "0", maximum = "100", example = "87", nullable = true)
    private Integer value;

    public MarkResponse(Mark entity) {
        super( entity.getId() );
        this.course = new CourseResponse( entity.getCourse() );
        this.user = new UserResponse( entity.getUser() );
        this.value = entity.getValue();
    }

    public CourseResponse getCourse() {
        return course;
    }

    public UserResponse getUser() {
        return user;
    }

    public Integer getValue() {
        return value;
    }
}
