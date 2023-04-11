package org.m.courses.service;

import org.m.courses.dao.AbstractDao;
import org.m.courses.dao.CourseDao;
import org.m.courses.dao.UserDao;
import org.m.courses.model.Course;
import org.m.courses.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CourseService extends AbstractService<Course> {

    private CourseDao courseDao;

    public CourseService(CourseDao courseDao) {
        this.courseDao = courseDao;
    }

    @Override
    protected AbstractDao<Course> getDao() {
        return courseDao;
    }

}
