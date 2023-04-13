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
    public void updateEntityTest() {
        Course entity = getService().create( buildNewEntity() );
        Course updatedEntity = buildEntity();

        updatedEntity.setId( entity.getId() );
        updatedEntity.setTeacher( entity.getTeacher() );

        Course entityFromDB = getService().update( updatedEntity );

        assertEntitiesEqual(updatedEntity, entityFromDB);
    }

    @Test
    public void getAsAdminTest() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        AuthManager.loginAs( admin );

        Course course = courseBuilder.setTeacher( userBuilder.toDB() ).toDB();

        assertEquals( course, courseService.get( course.getId() ) );
    }

    @Test
    public void createCourseThatDoesNotLeadTeacherTest() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        User user = userBuilder.setRole(Role.USER).toDB();

        IllegalArgumentException exception =
                assertThrowsExactly(IllegalArgumentException.class, () -> courseService.create( courseBuilder.setTeacher( admin ).buildNew() ) );
        assertEquals("only teacher can lead the course or it can be null", exception.getMessage() );

        exception = assertThrowsExactly(IllegalArgumentException.class, () -> courseService.create( courseBuilder.setTeacher( user ).buildNew() ) );
        assertEquals("only teacher can lead the course or it can be null", exception.getMessage() );

        Course course = courseService.create( courseBuilder.setTeacher( null ).buildNew() );
        assertNotNull( course );
    }

    @Test
    public void createAsAdminOrTeacherTest() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User user = userBuilder.setRole(Role.USER).toDB();

        AuthManager.loginAs( admin );
        courseService.create( courseBuilder.buildNew() );
        courseService.create( courseBuilder.setTeacher( teacher ).buildNew() );

        AuthManager.loginAs( teacher );
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.create( courseBuilder.buildNew() ));
        courseService.create( courseBuilder.setTeacher( teacher ).buildNew() );

        AuthManager.loginAs( user );
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.create( courseBuilder.buildNew() ));
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.create( buildNewEntity() ) );
    }

    @Test
    public void updateCourseThatDoesNotExistTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        Course courseToUpdate = courseBuilder.buildNew();

        courseToUpdate.setId( 1L );

        IllegalArgumentException ex = assertThrowsExactly(IllegalArgumentException.class, () -> courseService.update( courseToUpdate ));
        assertEquals(ex.getMessage(), "course does not exist");

        Course course = courseBuilder.setTeacher(teacher).buildNew();
        ex = assertThrowsExactly(IllegalArgumentException.class, () -> courseService.update( course ));
        assertEquals(ex.getMessage(), "course does not exist");
    }

    @Test
    public void updateCourseThatDoesNotHaveTeacherTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).toDB();

        Course courseToUpdate = getCourseToUpdate(null);

        AuthManager.loginAs( teacher );
        IllegalArgumentException ex = assertThrowsExactly(IllegalArgumentException.class, () -> courseService.update( courseToUpdate ));
        assertEquals(ex.getMessage(), "teacher cannot assign itself to course that has no owner");

        AuthManager.loginAs( admin );
        Course courseToUpdate2 = getCourseToUpdate(null);
        courseService.update( courseToUpdate2 );
    }

    @Test
    public void updateCourseThatHasTeacherTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).toDB();

        Course courseToUpdate1 = getCourseToUpdate(teacher);
        AuthManager.loginAs( teacher );

        IllegalArgumentException ex = assertThrowsExactly(IllegalArgumentException.class, () -> courseService.update( courseToUpdate1 ));
        assertEquals(ex.getMessage(), "teacher cannot assign itself to course that has no owner");

        AuthManager.loginAs( admin );

        Course courseToUpdate3 = getCourseToUpdate(teacher);
        Course updatedCourse = courseService.update( courseToUpdate3 );
        assertEquals( updatedCourse, courseToUpdate3 );
    }

    private Course getCourseToUpdate(User newTeacher) {
        Course course = courseBuilder.toDB();
        Course courseToUpdate = courseBuilder.buildNew();
        courseToUpdate.setId( course.getId() );
        courseToUpdate.setTeacher(newTeacher);
        return courseToUpdate;
    }

    @Test
    public void updateCourseChangeTeacherTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User courseOwnerTeacher = userBuilder.setRole(Role.TEACHER).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).toDB();

        Course course = courseBuilder.setTeacher(courseOwnerTeacher).toDB();

        AuthManager.loginAs( teacher );
        Course courseToUpdate = courseBuilder.setTeacher(teacher).buildNew();
        courseToUpdate.setId( course.getId() );

        IllegalArgumentException ex = assertThrowsExactly(IllegalArgumentException.class, () -> courseService.update( courseToUpdate ));
        assertEquals(ex.getMessage(), "teacher cannot change ownership of course to other");

        AuthManager.loginAs( admin );

        course = courseBuilder.setTeacher(courseOwnerTeacher).toDB();
        Course courseToUpdateAsAdmin = courseBuilder.setTeacher(teacher).buildNew();
        courseToUpdateAsAdmin.setId( course.getId() );
        Course updatedCourse = courseService.update( courseToUpdateAsAdmin );
        assertEquals( updatedCourse, courseToUpdateAsAdmin );
    }

    @Test
    public void updateCourseChangeNoCourseTeacherToTeacherTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).toDB();

        Course courseToUpdate = getCourseToUpdate(teacher);
        AuthManager.loginAs( teacher );

        IllegalArgumentException ex = assertThrowsExactly(IllegalArgumentException.class, () -> courseService.update( courseToUpdate ));
        assertEquals(ex.getMessage(), "teacher cannot assign itself to course that has no owner");

        AuthManager.loginAs( admin );

        Course courseToUpdateAsAdmin = getCourseToUpdate(teacher);
        Course updatedCourse = courseService.update( courseToUpdateAsAdmin );
        assertEquals( updatedCourse, courseToUpdateAsAdmin );
    }

    @Test
    public void updateCourseThatDoesNotLeadTeacherTest() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();

        Course courseToUpdate = getCourseToUpdate(admin);

        IllegalArgumentException exception =
                assertThrowsExactly(IllegalArgumentException.class, () -> courseService.update( courseToUpdate ) );
        assertEquals("only teacher can lead the course or it can be null", exception.getMessage() );

        Course courseToUpdateAdminTeacher = getCourseToUpdate(admin);

        exception = assertThrowsExactly(IllegalArgumentException.class, () -> courseService.update( courseToUpdateAdminTeacher ) );
        assertEquals("only teacher can lead the course or it can be null", exception.getMessage() );

        Course courseToUpdateNullTeacher = getCourseToUpdate(null);

        Course updatedCourse = courseService.update( courseToUpdateNullTeacher );
        assertNotNull( updatedCourse );
    }

    @Test
    public void updateCourseAsUserTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User user = userBuilder.setRole(Role.USER).toDB();

        Course course = courseBuilder.toDB();
        Course courseWithOwner = courseBuilder.setTeacher(teacher).toDB();

        AuthManager.loginAs( user );

        testUpdate(teacher, course);

        testUpdate(teacher, courseWithOwner);
    }

    private void testUpdate(User teacher, Course course) {
        Course courseToUpdate = courseBuilder.setTeacher(teacher).buildNew();
        courseToUpdate.setId( course.getId() );
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.update( courseToUpdate ));

        Course courseToUpdateNoOwner = courseBuilder.buildNew();
        courseToUpdateNoOwner.setId( course.getId() );
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.update( courseToUpdateNoOwner ));
    }

    @Test
    public void deleteAsNotAdminTest() {
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
