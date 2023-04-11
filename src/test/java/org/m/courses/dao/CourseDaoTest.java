package org.m.courses.dao;

import org.junit.jupiter.api.Test;
import org.m.courses.builder.CourseBuilder;
import org.m.courses.builder.UserBuilder;
import org.m.courses.model.Course;
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

    private void assertNotNullField(Course course, String fieldName) {
        DataIntegrityViolationException exception =
                assertThrowsExactly( DataIntegrityViolationException.class, () -> courseDao.create(course) );

        String detailedCause = exception.getMessage();

        assertEquals("could not execute statement; SQL [n/a]; constraint [null]; nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement", detailedCause);
    }
}

