package org.m.courses.dao;

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
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class CourseDaoTest extends AbstractDaoTest<Course>  {

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private CourseBuilder courseBuilder;

    @Autowired
    private UserBuilder userBuilder;

    protected AbstractDao<Course> getDao() {
        return courseDao;
    }

    @Override
    protected Course entityToDB() {
        return courseBuilder.setTeacher( userBuilder.toDB() ).toDB();
    }

    @Override
    protected Course buildEntity() {
        return courseBuilder.setTeacher( userBuilder.toDB() ).build();
    }

    @Override
    protected Course buildNewEntity() {
        return courseBuilder.setTeacher( userBuilder.toDB() ).buildNew();
    }

    @Override
    protected void assertEntitiesEqual(Course updatedCourse, Course courseFromDB) {
        assertEquals(updatedCourse.getTeacher(), courseFromDB.getTeacher());
        assertEquals(updatedCourse.getName(), courseFromDB.getName());
        assertEquals(updatedCourse.getDescription(), courseFromDB.getDescription());
        assertEquals(updatedCourse.getLessonCount(), courseFromDB.getLessonCount());
    }

    @Test
    void saveCourseWithNullFieldsTest() {
        Course course = courseBuilder.buildNew();
        course.setName(null);
        assertNotNullField(course, "name");
    }

    @Test
    void createAsTeacherCourseThatTeacherIsNullTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).toDB();

        AuthManager.loginAs( teacher );
        Course courseAsTeacher = courseBuilder.buildNew();
        assertThrowsExactly(AccessDeniedException.class, () -> courseDao.create( courseAsTeacher ) );

        AuthManager.loginAs( admin );
        Course course = courseBuilder.buildNew();
        Course createdCourse = courseDao.create( course );
        assertEquals( createdCourse, course );
    }

    private void assertNotNullField(Course course, String fieldName) {
        DataIntegrityViolationException exception =
                assertThrowsExactly( DataIntegrityViolationException.class, () -> courseDao.create(course) );

        String detailedCause = exception.getMessage();

        assertEquals("could not execute statement; SQL [n/a]; constraint [null]; nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement", detailedCause);
    }

    @Test
    void updateCourseWithNullFieldsTest() {
        Course course = entityToDB();
        Course newCourse = buildNewEntity();
        newCourse.setId( course.getId() );

        newCourse.setName(null);

        DataIntegrityViolationException exception =
                assertThrowsExactly( DataIntegrityViolationException.class, () -> courseDao.update(newCourse) );

        String detailedCause = exception.getMessage();

        assertEquals("could not execute statement; SQL [n/a]; constraint [null]; nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement", detailedCause);
    }

    @Test
    void deleteCourseTest() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        Course course = entityToDB();

        AuthManager.loginAs( teacher );
        assertThrowsExactly( AccessDeniedException.class, () -> courseDao.delete( course.getId() ) );

        AuthManager.loginAs( admin );
        courseDao.delete( course.getId() );
    }
}

