package org.m.courses.dao;

import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.Course;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.m.courses.repository.CourseRepository;
import org.m.courses.repository.PrimaryRepository;
import org.m.courses.repository.UserRepository;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.m.courses.filtering.specification.SpecificationUtil.buildEqualSpec;
import static org.springframework.data.jpa.domain.Specification.not;
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

    public Course get(Long id) {
        Specification<Course> equalIdSpec = buildEqualSpec("id", id);

        return getRepository().findOne( equalIdSpec.and( buildReadOnlySpec() ) )
                .orElse(null);
    }

    public Course create(Course course) {
//        if ( isAdmin() || (authorizationService.isTeacher() && authorizationService.getCurrentUser().equals( course.getTeacher() )) ) {
        Long teacherId = getTeacherId(course);
        if ( canModify( teacherId )) {
            return getRepository().save(course);
        }
        throw new AccessDeniedException();
    }

    @Override
    public Course update(Course course) {
        Long teacherId = getTeacherId(course);

        if ( !canModify( teacherId ) ) {
            throw new AccessDeniedException();
        }

        return getRepository().save(course);
    }

    private Long getTeacherId(Course course) {
        User teacher = course.getTeacher();
        return teacher != null ? teacher.getId() : null;
    }

    @Override
    public void delete(Long id) {
        Long teacherId = getTeacherId( get(id) );

        if ( !canModify( teacherId ) ) {
            throw new AccessDeniedException();
        }
        repository.deleteById(id);
    }

    @Override
    protected PrimaryRepository<Course, Long> getRepository() {
        return repository;
    }

    @Override
    protected boolean canModify(Long teacherId) {
        return super.canModify(teacherId) || (authorizationService.isTeacher() && authorizationService.getCurrentUser().getId().equals( teacherId ));
    }

    private Specification<Course> buildReadOnlySpec() {
//        if ( isAdmin() ) {
            return where( null );
//        } else if ( authorizationService.isTeacher() ) {
//            return buildEqualSpec( "teacher", authorizationService.getCurrentUser().getId() );
//        }
//        throw new AccessDeniedException();
//        return (root, cq, cb) -> cb.isNotNull( root.get("teacher") );
    }
}
