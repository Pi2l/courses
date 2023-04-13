package org.m.courses.service;

import org.junit.jupiter.api.Test;
import org.m.courses.auth.AuthManager;
import org.m.courses.builder.CourseBuilder;
import org.m.courses.builder.UserBuilder;
import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.Course;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CourseServiceTest extends AbstractServiceTest<Course> {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserBuilder userBuilder;

    @Autowired
    private CourseBuilder courseBuilder;

    @Test
    @Override
    void updateEntityTest() {
        Course entity = getService().create( buildNewEntity() );
        Course updatedEntity = buildEntity();

        updatedEntity.setId( entity.getId() );
        updatedEntity.setTeacher( entity.getTeacher() );

        Course entityFromDB = getService().update( updatedEntity );

        assertEntitiesEqual(updatedEntity, entityFromDB);
    }

    @Test
    void getAsAdminTest() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        AuthManager.loginAs( admin );

        Course course = courseBuilder.setTeacher( userBuilder.toDB() ).toDB();

        assertEquals( course, courseService.get( course.getId() ) );
    }

    @Test
    void createCourseThatDoesNotLeadTeacherTest() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        User user = userBuilder.setRole(Role.USER).toDB();

        AuthManager.loginAs( admin );

        IllegalArgumentException exception =
                assertThrowsExactly(IllegalArgumentException.class, () -> courseService.create( courseBuilder.setTeacher( admin ).buildNew() ) );
        assertEquals("only teacher can lead the course", exception.getMessage() );

        exception = assertThrowsExactly(IllegalArgumentException.class, () -> courseService.create( courseBuilder.setTeacher( user ).buildNew() ) );
        assertEquals("only teacher can lead the course", exception.getMessage() );

        Course course = courseService.create( courseBuilder.setTeacher( null ).buildNew() );
        assertNotNull( course );
    }

    @Test
    void createAsAdminOrTeacherTest() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User user = userBuilder.setRole(Role.USER).toDB();

        AuthManager.loginAs( admin );
        courseService.create( buildNewEntity() );

        AuthManager.loginAs( teacher );
        courseService.create( courseBuilder.setTeacher( teacher ).buildNew() );

        AuthManager.loginAs( user );
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.create( buildNewEntity() ) );
    }

    //move to dao test?
    @Test
    void createAsTeacherCourseThatTeacherIsNullTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();

        AuthManager.loginAs( teacher );

        assertThrowsExactly(AccessDeniedException.class, () -> courseService.create( courseBuilder.buildNew() ) );
    }

    @Test
    void updateAsTeacherCourseThatDoesNotHaveTeacherTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).toDB();

        Course course = courseBuilder.toDB();
        Course courseToUpdate = courseBuilder.buildNew();
        courseToUpdate.setId( course.getId() );

        AuthManager.loginAs( teacher );
        IllegalArgumentException ex = assertThrowsExactly(IllegalArgumentException.class, () -> courseService.update( courseToUpdate ));
        assertEquals(ex.getMessage(), "teacher cannot assign itself to course that has no owner");

        Course courseToUpdate1 = courseBuilder.buildNew();
        courseToUpdate1.setId( course.getId() );
        courseToUpdate1.setTeacher( teacher );

        ex = assertThrowsExactly(IllegalArgumentException.class, () -> courseService.update( courseToUpdate1 ));
        assertEquals(ex.getMessage(), "teacher cannot assign itself to course that has no owner");

        AuthManager.loginAs( admin );
        course = courseBuilder.toDB();
        Course courseToUpdate2 = courseBuilder.buildNew();
        courseToUpdate2.setId( course.getId() );
        courseService.update( courseToUpdate2 );

        course = courseBuilder.toDB();
        Course courseToUpdate3 = courseBuilder.buildNew();
        courseToUpdate3.setId( course.getId() );
        courseToUpdate3.setTeacher( teacher );
        courseService.update( courseToUpdate3 );
    }

    @Test
    void deleteAsNotAdminTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        Course course = courseBuilder.toDB();

        AuthManager.loginAs( teacher );
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.delete( course.getId() ) );

        AuthManager.loginAs( admin );
        courseService.delete( course.getId() );
        assertNull( courseService.get( course.getId() ) );
    }

    @Override
    protected AbstractService<Course> getService() {
        return courseService;
    }

    @Override
    protected Course entityToDB() {
        return courseBuilder.setTeacher( userBuilder.setRole(Role.TEACHER).toDB() ).toDB();
    }

    @Override
    protected Course buildEntity() {
        return courseBuilder.setTeacher( userBuilder.setRole(Role.TEACHER).toDB() ).build();
    }

    @Override
    protected Course buildNewEntity() {
        return courseBuilder.setTeacher( userBuilder.setRole(Role.TEACHER).toDB() ).buildNew();
    }

    @Override
    protected void assertEntitiesEqual(Course e1, Course e2) {

    }
}
