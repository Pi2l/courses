package org.m.courses.service;

import org.m.courses.builder.GroupBuilder;
import org.m.courses.model.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GroupServiceTest extends AbstractServiceTest<Group> {

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupBuilder groupBuilder;

    @Override
    protected AbstractService<Group> getService() {
        return groupService;
    }

    @Override
    protected Group entityToDB() {
        return groupBuilder.toDB();
    }

    @Override
    protected Group buildEntity() {
        return groupBuilder.build();
    }

    @Override
    protected Group buildNewEntity() {
        return groupBuilder.buildNew();
    }

    @Override
    protected void assertEntitiesEqual(Group e1, Group e2) {
    }
}
