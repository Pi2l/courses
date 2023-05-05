package org.m.courses.api.v1.controller.group;

import org.m.courses.api.v1.controller.common.AbstractRequest;
import org.m.courses.model.Group;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Set;

public class GroupRequest extends AbstractRequest<Group> {

    @NotBlank
    private String name;
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
