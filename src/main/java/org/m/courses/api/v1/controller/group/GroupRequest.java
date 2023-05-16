package org.m.courses.api.v1.controller.group;

import io.swagger.v3.oas.annotations.media.Schema;
import org.m.courses.api.v1.controller.common.AbstractRequest;
import org.m.courses.model.Group;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.util.Set;

public class GroupRequest extends AbstractRequest<Group> {

    @Schema(description = "Name", example = "name1")
    @NotBlank
    private String name;

    @Schema(description = "Course ids", example = "[1,2]")
    private Set<@PositiveOrZero Long> courseIds;

    public GroupRequest() {
    }

    public GroupRequest(Group group) {
        this.setName( group.getName() );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Group createEntity() {
        Group entity = new Group();
        return updateEntity(entity);
    }

    @Override
    public Group updateEntity(Group entity) {
        entity.setName( getName() );
        return entity;
    }

    public Set<Long> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(Set<Long> courseIds) {
        this.courseIds = courseIds;
    }
}
