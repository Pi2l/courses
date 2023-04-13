package org.m.courses.builder;

import org.m.courses.dao.CourseDao;
import org.m.courses.model.Course;
import org.m.courses.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;


@Component
public class CourseBuilder {

    @Autowired private CourseDao courseDao;

    private User teacher;

    private Course course;

    public static CourseBuilder builder() {
        return new CourseBuilder();
    }

    public CourseBuilder() {
        initDefaultUser();
    }

    public Course build() {
        Course odlUser = course;
        initDefaultUser();
        return odlUser;
    }

    public Course buildNew() {
        return setId(null)
                .build();
    }

    // Spring based
    public Course toDB() {
        return courseDao.create( buildNew() );
    }

    private CourseBuilder initDefaultUser() {
        long randomValue = Math.abs(new SecureRandom().nextLong()) % 10000;
        this.course = new Course();

        setId(randomValue);
        setTeacher(teacher);
        setName("Name_" + randomValue);
        setDescription("Description_" + randomValue);
        setLessonCount((int) randomValue);
        return this;
    }

    public CourseBuilder setId(Long id) {
        course.setId(id);
        return this;
    }

    public CourseBuilder setTeacher(User user) {
        course.setTeacher(user);
        return this;
    }

    public CourseBuilder setName(String str) {
        course.setName(str);
        return this;
    }

    public CourseBuilder setDescription(String str) {
        course.setDescription(str);
        return this;
    }

    public CourseBuilder setLessonCount(Integer str) {
        course.setLessonCount(str);
        return this;
    }
}
