package org.m.courses.service;

import org.m.courses.builder.CourseBuilder;
import org.m.courses.builder.GroupBuilder;
import org.m.courses.model.Course;
import org.m.courses.model.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

@SpringBootTest
public class GroupServiceTest extends AbstractServiceTest<Group> {

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupBuilder groupBuilder;

    @Autowired
    private CourseBuilder courseBuilder;
    @Override
    protected AbstractService<Group> getService() {
        return groupService;
    }

    @Override
    protected Group entityToDB() {
        return getGroupBuilder().toDB();
    }

    @Override
    protected Group buildEntity() {
        return getGroupBuilder().build();
    }

    @Override
    protected Group buildNewEntity() {
        return getGroupBuilder().buildNew();
    }

    private GroupBuilder getGroupBuilder() {
        Course course1 = courseBuilder.toDB();
        Course course2 = courseBuilder.toDB();

        return groupBuilder.setCourses( Set.of(course1, course2) );
    }

    @Override
    protected void assertEntitiesEqual(Group e1, Group e2) {
    }
}
