package org.m.courses.dao;

import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.Course;
import org.m.courses.model.Group;
import org.m.courses.model.User;
import org.m.courses.repository.CourseRepository;
import org.m.courses.repository.PrimaryRepository;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.OneToMany;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.SetJoin;
import java.util.List;
import java.util.Set;

import static org.m.courses.filtering.specification.SpecificationUtil.buildEqualSpec;
import static org.springframework.data.jpa.domain.Specification.where;

@Component
public class CourseDao extends AbstractDao<Course> {

    private final CourseRepository repository;

    public CourseDao(CourseRepository repository, UserAuthorizationService authorizationService) {
        super(authorizationService);
        this.repository = repository;
    }

    @Override
    public List<Course> getAll() {
        return getRepository().findAll( buildReadOnlySpec() );
    }

    @Override
    public Page<Course> getAll(Pageable pageable, Specification<Course> filter) {
        return getRepository().findAll( buildReadOnlySpec().and( filter ), pageable );
    }

    @Override
    public Course get(Long id) {
        Specification<Course> equalIdSpec = buildEqualSpec("id", id);

        return getRepository().findOne( equalIdSpec.and( buildReadOnlySpec() ) )
                .orElse(null);
    }

    @Override
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
            throw new AccessDeniedException();
        }
        super.delete( id );
    }

    @Override
    protected PrimaryRepository<Course, Long> getRepository() {
        return repository;
    }

    @Override
    protected boolean canModify(Long courseId) {
        Course course = get(courseId);
        return super.canModify(courseId)
                || authorizationService.getCurrentUser().equals( course == null ? null : course.getTeacher() );
    }

    private Specification<Course> buildReadOnlySpec() {
        if ( authorizationService.isTeacher() ) {
            return where( buildEqualSpec("teacher", authorizationService.getCurrentUser().getId()) );
        } else if ( authorizationService.isUser() ) {
            return where( getUserEnrolledCourses() );
        }
        return where( null );
    }

    private Specification<Course> getUserEnrolledCourses() {
        return (root, cq, cb) -> {
            SetJoin<Course, Group> groupJoin = root.joinSet("groups");
            Join<Group, User> userJoin = groupJoin.joinSet("users");

            return cb.equal( userJoin.get("id"), authorizationService.getCurrentUser().getId() );
        };
    }
}
