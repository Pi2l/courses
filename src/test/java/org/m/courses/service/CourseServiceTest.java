package org.m.courses.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.m.courses.auth.AuthManager;
import org.m.courses.builder.CourseBuilder;
import org.m.courses.builder.GroupBuilder;
import org.m.courses.dao.CourseDao;
import org.m.courses.dao.GroupDao;
import org.m.courses.dao.UserDao;
import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.Course;
import org.m.courses.model.Group;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CourseServiceTest extends AbstractServiceTest<Course> {


    @Autowired
    private GroupBuilder groupBuilder;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseBuilder courseBuilder;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private UserDao userDao;

    @AfterEach
    void cleanDB() {
        AuthManager.loginAs( userBuilder.setRole(Role.ADMIN).build() );

        userDao.getAll().forEach( el -> { el.setGroup(null); userDao.update( el ); } );
        groupDao.getAll().forEach( el -> groupDao.delete( el.getId() ) );
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

        Course courseOwnsAdmin = courseBuilder.setTeacher( admin ).buildNew();
        IllegalArgumentException exception =
                assertThrowsExactly(IllegalArgumentException.class, () -> courseService.create( courseOwnsAdmin ) );
        assertEquals("only teacher can lead the course or it can be null", exception.getMessage() );

        Course courseOwnsUser = courseBuilder.setTeacher( user ).buildNew();
        exception = assertThrowsExactly(IllegalArgumentException.class, () -> courseService.create( courseOwnsUser ) );
        assertEquals("only teacher can lead the course or it can be null", exception.getMessage() );

        Course course = courseService.create( courseBuilder.setTeacher( null ).buildNew() );
        assertNotNull( course );
    }

    @Test
    void createAsAdminOrTeacherTest() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User user = userBuilder.setRole(Role.USER).toDB();

        AuthManager.loginAs( admin );
        courseService.create( courseBuilder.buildNew() );
        courseService.create( courseBuilder.setTeacher( teacher ).buildNew() );

        Course courseOwnerNull = courseBuilder.buildNew();
        AuthManager.loginAs( teacher );
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.create( courseOwnerNull ));
        courseService.create( courseBuilder.setTeacher( teacher ).buildNew() );

        AuthManager.loginAs( admin );
        Course courseOwnerNull1 = courseBuilder.buildNew();
        Course courseOwnerTeacher = courseBuilder.setTeacher( teacher ).buildNew();
        AuthManager.loginAs( user );
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.create( courseOwnerNull1 ) );
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.create( courseOwnerTeacher ) );
    }

    @Test
    void updateCourseThatDoesNotHaveTeacherTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).toDB();

        Course courseToUpdate = getCourseToUpdate(null);
        Course courseToUpdate1 = getCourseToUpdate(teacher);

        AuthManager.loginAs( teacher );
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.update( courseToUpdate ));
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.update( courseToUpdate1 ));

        AuthManager.loginAs( admin );
        Course courseToUpdate2 = getCourseToUpdate(null);
        Course courseToUpdate3 = getCourseToUpdate(teacher);

        Course updatedCourse = courseService.update( courseToUpdate2 );
        Course updatedCourse1 = courseService.update( courseToUpdate3 );
        assertEquals( updatedCourse, courseToUpdate2 );
        assertEquals( updatedCourse1, courseToUpdate3 );
    }

    private Course getCourseToUpdate(User newTeacher) {
        Course course = courseBuilder.toDB();
        Course courseToUpdate = courseBuilder.buildNew();
        courseToUpdate.setId( course.getId() );
        courseToUpdate.setTeacher(newTeacher);
        return courseToUpdate;
    }

    @Test
    void updateCourseChangeTeacherTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User courseOwnerTeacher = userBuilder.setRole(Role.TEACHER).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).toDB();

        Course course = courseBuilder.setTeacher(courseOwnerTeacher).toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();

        AuthManager.loginAs( teacher );
        Course courseToUpdate = courseBuilder.setTeacher(teacher).buildNew();
        courseToUpdate.setId( course.getId() );

        assertThrowsExactly(AccessDeniedException.class, () -> courseService.update( courseToUpdate ));

        AuthManager.loginAs( admin );

        course = courseBuilder.setTeacher(courseOwnerTeacher).toDB();
        Course courseToUpdateAsAdmin = courseBuilder.setTeacher(teacher).buildNew();
        courseToUpdateAsAdmin.setId( course.getId() );
        Course updatedCourse = courseService.update( courseToUpdateAsAdmin );
        assertEquals( updatedCourse, courseToUpdateAsAdmin );
    }

    @Test
    void updateCourseThatDoesNotLeadTeacherTest() {
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
    void updateCourseAsUserTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();

        Course course = courseBuilder.toDB();
        Course courseWithOwner = courseBuilder.setTeacher(teacher).toDB();

        Group group = groupBuilder.setCourses( Set.of(course, courseWithOwner) ).toDB();
        User user = userBuilder.setRole(Role.USER).setGroup(group).toDB();

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
    void deleteAsNotAdminTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).toDB();

        Course course = courseBuilder.setTeacher(teacher).toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();
        User user = userBuilder.setRole(Role.USER).setGroup( group ).toDB();

        AuthManager.loginAs( teacher );
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.delete( course.getId() ) );

        AuthManager.loginAs( user );
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.delete( course.getId() ) );

        AuthManager.loginAs( admin );
        courseService.delete( course.getId() );
        assertNull( courseService.get( course.getId() ) );
    }

    @Test
    void deleteCourseByTeacherTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        Course course = courseBuilder.toDB();
        Course courseOwnsTeacher = courseBuilder.setTeacher( teacher ).toDB();

        AuthManager.loginAs( teacher );
        assertThrowsExactly(AccessDeniedException.class, () -> courseService.delete( course.getId() ) );
        courseService.delete( courseOwnsTeacher.getId() );
        assertNull( courseService.get( courseOwnsTeacher.getId() ) );

        AuthManager.loginAs( admin );
        courseOwnsTeacher = courseBuilder.setTeacher( teacher ).toDB();

        courseService.delete( course.getId() );
        courseService.delete( courseOwnsTeacher.getId() );
        assertNull( courseService.get( course.getId() ) );
        assertNull( courseService.get( courseOwnsTeacher.getId() ) );
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
