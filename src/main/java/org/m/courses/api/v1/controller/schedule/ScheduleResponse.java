package org.m.courses.api.v1.controller.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import org.m.courses.api.v1.controller.common.AbstractResponse;
import org.m.courses.api.v1.controller.course.CourseResponse;
import org.m.courses.api.v1.controller.group.GroupResponse;
import org.m.courses.model.Schedule;

import java.time.ZonedDateTime;

public class ScheduleResponse extends AbstractResponse {

    private CourseResponse course;

    @Schema(description = "Start at", example = "2023-04-28T13:15:25.25+03:00[Europe/Kyiv]", format = "date-time")
    private ZonedDateTime startAt;

    @Schema(description = "End at", example = "2024-04-28T13:15:25.25+03:00[Europe/Kyiv]", format = "date-time")
    private ZonedDateTime endAt;
    private GroupResponse group;

    public ScheduleResponse(Schedule entity) {
        super( entity.getId() );
        this.course = new CourseResponse( entity.getCourse() );
        this.startAt = entity.getStartAt();
        this.endAt = entity.getEndAt();
        this.group = new GroupResponse( entity.getGroup() );
    }

    public CourseResponse getCourse() {
        return course;
    }

    public ZonedDateTime getStartAt() {
        return startAt;
    }

    public ZonedDateTime getEndAt() {
        return endAt;
    }

    public GroupResponse getGroup() {
        return group;
    }
}
