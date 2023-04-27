package org.m.courses.dao;

import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.Group;
import org.m.courses.repository.GroupRepository;
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
public class GroupDao extends AbstractDao<Group> {

    private final GroupRepository repository;

    public GroupDao(GroupRepository repository, UserAuthorizationService authorizationService) {
        super(authorizationService);
        this.repository = repository;
    }

    @Override
    public List<Group> getAll() {
        return getRepository().findAll( buildReadOnlySpec() );
    }

    @Override
    public Page<Group> getAll(Pageable pageable, Specification<Group> filter) {
        return getRepository().findAll( buildReadOnlySpec().and( filter ), pageable );
    }

    public Group get(Long id) {
        Specification<Group> equalIdSpec = buildEqualSpec("id", id);

        return getRepository().findOne( equalIdSpec.and( buildReadOnlySpec() ) )
                .orElse(null);
    }

    @Override
    public Group update(Group group) {
        if ( !canModify( group.getId() ) ) {
            throw new AccessDeniedException();
        }

        return getRepository().save(group);
    }

    @Override
    public void delete(Long id) {
        if ( !canModify( id ) ) {
            throw new AccessDeniedException();
        }
        repository.deleteById(id);
    }

    @Override
    protected PrimaryRepository<Group, Long> getRepository() {
        return repository;
    }

    @Override
    protected boolean canModify(Long id) {
        return super.canModify(id);
    }

    private Specification<Group> buildReadOnlySpec() {
        return where( null );
    }
}
