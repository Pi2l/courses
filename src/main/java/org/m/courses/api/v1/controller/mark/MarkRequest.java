package org.m.courses.api.v1.controller.mark;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.Range;
import org.m.courses.api.v1.controller.common.AbstractRequest;
import org.m.courses.model.Mark;
import org.m.courses.model.Schedule;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

public class MarkRequest extends AbstractRequest<Mark> {

    @Schema(description = "Course id", example = "1")
    @NotNull
    private Long courseId;

    @Schema(description = "User id", example = "1")
    @NotNull
    private Long userId;

    @Schema(description = "Mark value", minimum = "0", maximum = "100", example = "87", nullable = true)
    @Range(min = 0, max = 100)
    private Integer value;

    public MarkRequest() {
    }

    @Override
    public Mark createEntity() {
        Mark entity = new Mark();
        return updateEntity(entity);
    }

    @Override
    public Mark updateEntity(Mark entity) {
        entity.setValue( value );
        return entity;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
