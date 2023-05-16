package org.m.courses.service;

import org.m.courses.dao.AbstractDao;
import org.m.courses.dao.CourseDao;
import org.m.courses.model.Course;
import org.m.courses.model.Role;
import org.springframework.stereotype.Service;

@Service
public class CourseService extends AbstractService<Course> {

    private final CourseDao courseDao;
    private final UserAuthorizationService authorizationService;

    public CourseService(CourseDao courseDao, UserAuthorizationService authorizationService) {
        this.courseDao = courseDao;
        this.authorizationService = authorizationService;
    }

    @Override
    protected AbstractDao<Course> getDao() {
        return courseDao;
    }

    @Override
    public Course create(Course course) {
        validate(course);
        return super.create( course );
    }

    private void validate(Course course) {
        if ( course.getTeacher() != null && !Role.TEACHER.equals( course.getTeacher().getRole() ) ) {
            throw new IllegalArgumentException("only teacher can lead the course or it can be null");
        }
    }

    @Override
    public Course update(Course course) {
        validate(course);
        return super.update( course );
    }
}
