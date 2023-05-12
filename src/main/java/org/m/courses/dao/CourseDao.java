package org.m.courses.dao;

import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.Course;
import org.m.courses.repository.CourseRepository;
import org.m.courses.repository.PrimaryRepository;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.stereotype.Component;

@Component
public class CourseDao extends AbstractDao<Course> {

    private final CourseRepository repository;

    public CourseDao(CourseRepository repository, UserAuthorizationService authorizationService) {
        super(authorizationService);
        this.repository = repository;
    }

    public Course create(Course course) {
        if ( isAdmin()
                || authorizationService.isTeacher() && authorizationService.getCurrentUser().equals( course.getTeacher() ) ) {
            return getRepository().save(course);
        }
        throw new AccessDeniedException();
    }

    @Override
    public void delete(Long id) {
        Course course = get(id);
        if (course == null) {
            return;
        }
        super.delete( id );
    }

    @Override
    protected PrimaryRepository<Course, Long> getRepository() {
        return repository;
    }

    @Override
    protected boolean canModify(Long courseId) {
        return super.canModify(courseId)
                || authorizationService.getCurrentUser().equals( get( courseId ).getTeacher() );
    }

    // if current user is USER: only users that is in group that has such course, can get it
    // if current user is TEACHER: can get only its own courses
}
