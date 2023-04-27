package org.m.courses.api.v1.controller.schedule;

import org.m.courses.api.v1.controller.common.AbstractRequest;
import org.m.courses.model.Schedule;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

public class ScheduleRequest extends AbstractRequest<Schedule> {

    @NotNull
    private Long courseId;

    @NotNull
    private ZonedDateTime startAt;

    @NotNull
    private ZonedDateTime endAt;

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
