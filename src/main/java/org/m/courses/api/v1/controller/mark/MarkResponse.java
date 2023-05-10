package org.m.courses.api.v1.controller.mark;

import org.m.courses.api.v1.controller.common.AbstractResponse;
import org.m.courses.api.v1.controller.course.CourseResponse;
import org.m.courses.api.v1.controller.group.GroupResponse;
import org.m.courses.api.v1.controller.user.UserResponse;
import org.m.courses.model.Mark;

public class MarkResponse extends AbstractResponse {

    private CourseResponse course;
    private UserResponse user;
    private Integer value;

    public MarkResponse(Mark entity) {
        super( entity.getId() );
        this.course = new CourseResponse( entity.getCourse() );
        this.user = new UserResponse( entity.getUser() );
        this.value = entity.getValue();
    }
}
