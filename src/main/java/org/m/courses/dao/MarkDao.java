package org.m.courses.dao;

import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.Course;
import org.m.courses.model.Mark;
import org.m.courses.model.User;
import org.m.courses.repository.MarkRepository;
import org.m.courses.repository.PrimaryRepository;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.m.courses.filtering.specification.SpecificationUtil.buildEqualSpec;
import static org.springframework.data.jpa.domain.Specification.where;

@Component
public class MarkDao extends AbstractDao<Mark> {

    private final MarkRepository repository;

    public MarkDao(MarkRepository repository, UserAuthorizationService authorizationService) {
        super(authorizationService);
        this.repository = repository;
    }

    @Override
    public List<Mark> getAll() {
        return getRepository().findAll( buildReadOnlySpec() );
    }

    @Override
    public Page<Mark> getAll(Pageable pageable, Specification<Mark> filter) {
        return getRepository().findAll( buildReadOnlySpec().and( filter ), pageable );
    }

    public Mark get(Long id) {
        Specification<Mark> equalIdSpec = buildEqualSpec("id", id);

        return getRepository().findOne( equalIdSpec.and( buildReadOnlySpec() ) )
                .orElse(null);
    }

    public Mark create(Mark entity) {
        Course course = entity.getCourse();

        if ( canModify( course ) ) {
            return getRepository().save(entity);
        }

        throw new AccessDeniedException();
    }

    @Override
    protected PrimaryRepository<Mark, Long> getRepository() {
        return repository;
    }

    @Override
    protected boolean canModify(Long id) {
        Course course = get( id ).getCourse();
        return canModify( course );
    }

    private boolean canModify(Course course) {
        User teacher = course == null ? null : course.getTeacher();
        return isAdmin()
                || teacher != null && teacher.equals(authorizationService.getCurrentUser());
    }

    private Specification<Mark> buildReadOnlySpec() {
        return where( null );
    }
}
