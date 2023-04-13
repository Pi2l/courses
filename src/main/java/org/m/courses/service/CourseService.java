package org.m.courses.service;

import org.m.courses.dao.AbstractDao;
import org.m.courses.dao.CourseDao;
import org.m.courses.model.Course;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.springframework.stereotype.Service;

@Service
public class CourseService extends AbstractService<Course> {

    private CourseDao courseDao;

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
        User user = course.getTeacher();
        hasTeacherRoleOrIsNull(user);
        return super.create( course );
    }

    private void hasTeacherRoleOrIsNull(User user) {
        if ( !(user == null || Role.TEACHER.equals( user.getRole() )) ) {
            throw new IllegalArgumentException("only teacher can lead the course or it can be null");
        }
    }

    @Override
    public Course update(Course course) {
        Course courseFromDB = get( course.getId() );
        if (courseFromDB != null) {
            User courseFromDBTeacher = courseFromDB.getTeacher();
            User courseTeacher = course.getTeacher();

            if (!authorizationService.isAdmin() && courseFromDBTeacher != null && courseTeacher != null &&
                    !courseTeacher.equals( courseFromDBTeacher ) ) {
                throw new IllegalArgumentException("teacher cannot change ownership of course to other");
            }
            if (authorizationService.isTeacher() && !authorizationService.getCurrentUser().equals(courseFromDBTeacher)) {
                throw new IllegalArgumentException("teacher cannot assign itself to course that has no owner");
            }

            hasTeacherRoleOrIsNull(courseTeacher);
            return super.update( course );
        } else {
            throw new IllegalArgumentException("course does not exist");
        }
    }
}
