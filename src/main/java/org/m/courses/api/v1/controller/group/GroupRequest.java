package org.m.courses.api.v1.controller.group;

import org.m.courses.api.v1.controller.common.AbstractRequest;
import org.m.courses.model.Group;

import javax.validation.constraints.NotBlank;
import java.util.Set;

public class GroupRequest extends AbstractRequest<Group> {

    @NotBlank
    private String name;

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
}
