package org.m.courses.api.v1.controller.group;

import org.m.courses.api.v1.controller.common.AbstractResponse;
import org.m.courses.model.Group;

public class GroupResponse extends AbstractResponse {

    private String name;

    public GroupResponse(Group entity) {
        super( entity.getId() );
        this.name = entity.getName();
    }
}
