package org.m.courses.api.v1.controller.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import org.m.courses.api.v1.controller.common.AbstractRequest;
import org.m.courses.model.Schedule;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

public class ScheduleRequest extends AbstractRequest<Schedule> {

    @Schema(description = "Course id", example = "1")
    @NotNull
    private Long courseId;

    @Schema(description = "Start at", example = "2023-04-28T13:15:25.25+03:00[Europe/Kyiv]", format = "date-time")
    @NotNull
    private ZonedDateTime startAt;

    @Schema(description = "End at", example = "2024-04-28T13:15:25.25+03:00[Europe/Kyiv]", format = "date-time")
    @NotNull
    private ZonedDateTime endAt;

    @Schema(description = "Group id", example = "1")
    @NotNull
    private Long groupId;

    public ScheduleRequest() {
    }

    @Override
    public Schedule createEntity() {
        Schedule entity = new Schedule();
        return updateEntity(entity);
    }

    @Override
    public Schedule updateEntity(Schedule entity) {
        entity.setStartAt( getStartAt() );
        entity.setEndAt( getEndAt() );
        return entity;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public ZonedDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(ZonedDateTime startAt) {
        this.startAt = startAt;
    }

    public ZonedDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(ZonedDateTime endAt) {
        this.endAt = endAt;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
