package org.m.courses.dao;

import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.Group;
import org.m.courses.model.Role;
import org.m.courses.model.Schedule;
import org.m.courses.repository.GroupRepository;
import org.m.courses.repository.PrimaryRepository;
import org.m.courses.repository.ScheduleRepository;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.m.courses.filtering.specification.SpecificationUtil.buildEqualSpec;
import static org.springframework.data.jpa.domain.Specification.not;
import static org.springframework.data.jpa.domain.Specification.where;

@Component
public class ScheduleDao extends AbstractDao<Schedule> {

    private final ScheduleRepository repository;

    public ScheduleDao(ScheduleRepository repository, UserAuthorizationService authorizationService) {
        super(authorizationService);
        this.repository = repository;
    }

    @Override
    public List<Schedule> getAll() {
        return getRepository().findAll( buildReadOnlySpec() );
    }

    @Override
    public Page<Schedule> getAll(Pageable pageable, Specification<Schedule> filter) {
        return getRepository().findAll( buildReadOnlySpec().and( filter ), pageable );
    }

    public Schedule get(Long id) {
        Specification<Schedule> equalIdSpec = buildEqualSpec("id", id);

        return getRepository().findOne( equalIdSpec.and( buildReadOnlySpec() ) )
                .orElse(null);
    }

    @Override
    public Schedule update(Schedule schedule) {
        if ( !canModify( schedule.getId() ) ) {
            throw new AccessDeniedException();
        }

        return getRepository().save(schedule);
    }

    @Override
    public void delete(Long id) {
        if ( !canModify( id ) ) {
            throw new AccessDeniedException();
        }
        repository.deleteById(id);
    }

    @Override
    protected PrimaryRepository<Schedule, Long> getRepository() {
        return repository;
    }

    @Override
    protected boolean canModify(Long id) {
        return super.canModify(id);
    }

    private Specification<Schedule> buildReadOnlySpec() {
        return where( null );
    }
}
