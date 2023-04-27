package org.m.courses.api.v1.controller.schedule;

import org.m.courses.api.v1.controller.common.AbstractResponse;
import org.m.courses.api.v1.controller.course.CourseResponse;
import org.m.courses.api.v1.controller.group.GroupResponse;
import org.m.courses.model.Course;
import org.m.courses.model.Group;
import org.m.courses.model.Schedule;
import org.m.courses.model.User;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

public class ScheduleResponse extends AbstractResponse {

    private CourseResponse course;
    private ZonedDateTime startAt;
    private ZonedDateTime endAt;
    private GroupResponse group;

    public ScheduleResponse(Schedule entity) {
        super( entity.getId() );
        this.course = new CourseResponse( entity.getCourse() );
        this.startAt = entity.getStartAt();
        this.endAt = entity.getEndAt();
        this.group = new GroupResponse( entity.getGroup() );
    }
}
