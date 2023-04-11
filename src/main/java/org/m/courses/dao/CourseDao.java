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

    @Override
    public Course update(Course course) {
        if ( !canModify( course.getTeacher().getId() ) ) {
            throw new AccessDeniedException();
        }

        return getRepository().save(course);
    }

    @Override
    public void delete(Long id) {
        if ( !canModify( get(id).getTeacher().getId() ) ) {
            throw new AccessDeniedException();
        }
        repository.deleteById(id);
    }

    @Override
    protected PrimaryRepository<Course, Long> getRepository() {
        return repository;
    }

    @Override
    protected boolean canModify(Long id) {
        return super.canModify(id) || authorizationService.getCurrentUser().getId().equals( id );
    }

    private Specification<Course> buildReadOnlySpec() {
        return where( null );
    }

//    private boolean isStudentOfThisCourse() {
//        return repository.findStudentById( authorizationService.getCurrentUser().getId() ).isPresent();
//    }
}
