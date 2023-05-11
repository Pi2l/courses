package org.m.courses.service;

import org.junit.jupiter.api.Test;
import org.m.courses.builder.CourseBuilder;
import org.m.courses.builder.GroupBuilder;
import org.m.courses.builder.MarkBuilder;
import org.m.courses.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@SpringBootTest
public class MarkServiceTest extends AbstractServiceTest<Mark> {

    @Autowired
    private MarkService markService;

    @Autowired
    private MarkBuilder markBuilder;

    @Autowired
    private CourseBuilder courseBuilder;

    @Autowired
    private GroupBuilder groupBuilder;

    @Override
    protected AbstractService<Mark> getService() {
        return markService;
    }

    @Override
    protected Mark entityToDB() {
        Course course = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();
        User user = userBuilder.setRole(Role.USER).setGroup( group ).toDB();
        return markBuilder
                .setCourse( course )
                .setUser( user )
                .toDB();
    }

    @Override
    protected Mark buildEntity() {
        Course course = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();
        User user = userBuilder.setRole(Role.USER).setGroup( group ).toDB();
        return markBuilder
                .setCourse( course )
                .setUser( user )
                .build();
    }

    @Override
    protected Mark buildNewEntity() {
        Course course = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();
        User user = userBuilder.setRole(Role.USER).setGroup( group ).toDB();
        return markBuilder
                .setCourse( course )
                .setUser( user )
                .buildNew();
    }

    @Override
    protected void assertEntitiesEqual(Mark e1, Mark e2) {
    }

    @Test
    void createMarkWithWrongUser() {
        Course course = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        Course otherCourse = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();
        Group otherGroup = groupBuilder.setCourses( Set.of(otherCourse) ).toDB();

        User user = userBuilder.setRole(Role.USER).setGroup(group).toDB();
        User userWithOtherGroup = userBuilder.setGroup(otherGroup).setRole(Role.USER).toDB();
        Mark mark = markBuilder
                .setCourse( course )
                .setUser( userWithOtherGroup ).toDB();

        Throwable ex = assertThrowsExactly(IllegalArgumentException.class, () -> markService.create( mark ) );
        assertEquals("user does not belong to that course", ex.getMessage());

        mark.setUser( user );
        Mark createdMark = markService.create( mark );
        assertEquals( createdMark, mark );
    }

    @Test
    void createMarkWithUserThatHasNotGroup() {
        Course course = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();

        User user = userBuilder.setRole(Role.USER).setGroup( null ).toDB();
        Mark mark = markBuilder
                .setCourse( course )
                .setUser( user ).toDB();

        Throwable ex = assertThrowsExactly(IllegalArgumentException.class, () -> markService.create( mark ) );
        assertEquals("user has to be in group", ex.getMessage());
    }

    @Test
    void createMarkWithUserThatHasGroupThatHasNotCourses() {
        Course course = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        Group group = groupBuilder.setCourses( null ).toDB();

        User user = userBuilder.setRole(Role.USER).setGroup( group ).toDB();
        Mark mark = markBuilder
                .setCourse( course )
                .setUser( user ).toDB();

        Throwable ex = assertThrowsExactly(IllegalArgumentException.class, () -> markService.create( mark ) );
        assertEquals("group has contain any course", ex.getMessage());
    }

    @Test
    void createMarkWithWrongUserRole() {
        Course course = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();
        User teacher = userBuilder.setRole(Role.TEACHER).setGroup(group).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).setGroup(group).toDB();

        Mark teacherMark = markBuilder
                .setCourse( course )
                .setUser( teacher ).buildNew();

        Mark adminMark = markBuilder
                .setCourse( course )
                .setUser( admin ).buildNew();

        Throwable ex = assertThrowsExactly(IllegalArgumentException.class, () -> markService.create( teacherMark ) );
        assertEquals("user must have USER role", ex.getMessage());

        ex = assertThrowsExactly(IllegalArgumentException.class, () -> markService.create( adminMark ) );
        assertEquals("user must have USER role", ex.getMessage());
    }

    @Test
    void updateMarkWithWrongUser() {
        Course course = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        Course otherCourse = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();
        Group otherGroup = groupBuilder.setCourses( Set.of(otherCourse) ).toDB();

        User user = userBuilder.setRole(Role.USER).setGroup(group).toDB();
        User userWithOtherGroup = userBuilder.setGroup(otherGroup).setRole(Role.USER).toDB();

        Mark mark = entityToDB();
        mark.setCourse( course );
        mark.setUser( userWithOtherGroup );

        Throwable ex = assertThrowsExactly(IllegalArgumentException.class, () -> markService.update( mark ) );
        assertEquals("user does not belong to that course", ex.getMessage());

        mark.setUser( user );
        Mark updatedMark = markService.update( mark );
        assertEquals( updatedMark, mark );
    }

    @Test
    void updateMarkWithWrongUserRole() {
        Course course = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();
        User teacher = userBuilder.setRole(Role.TEACHER).setGroup(group).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).setGroup(group).toDB();

        Mark teacherMark = entityToDB();
        teacherMark.setCourse( course );
        teacherMark.setUser( teacher );

        Mark adminMark = entityToDB();
        adminMark.setCourse( course );
        adminMark.setUser( admin );

        Throwable ex = assertThrowsExactly(IllegalArgumentException.class, () -> markService.update( teacherMark ) );
        assertEquals("user must have USER role", ex.getMessage());

        ex = assertThrowsExactly(IllegalArgumentException.class, () -> markService.update( adminMark ) );
        assertEquals("user must have USER role", ex.getMessage());
    }

    @Test
    void updateMarkWithUserThatHasNotGroup() {
        Course course = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();

        User user = userBuilder.setRole(Role.USER).setGroup( null ).toDB();
        Mark mark = entityToDB();
        mark.setCourse( course );
        mark.setUser( user );

        Throwable ex = assertThrowsExactly(IllegalArgumentException.class, () -> markService.update( mark ) );
        assertEquals("user has to be in group", ex.getMessage());
    }

    @Test
    void updateMarkWithUserThatHasGroupThatHasNotCourses() {
        Course course = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        Group group = groupBuilder.setCourses( null ).toDB();

        User user = userBuilder.setRole(Role.USER).setGroup( group ).toDB();
        Mark mark = entityToDB();
        mark.setCourse( course );
        mark.setUser( user );

        Throwable ex = assertThrowsExactly(IllegalArgumentException.class, () -> markService.update( mark ) );
        assertEquals("group has contain any course", ex.getMessage());
    }
}
