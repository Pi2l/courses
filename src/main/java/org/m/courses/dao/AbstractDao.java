package org.m.courses.dao;

import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.Identity;
import org.m.courses.model.Role;
import org.m.courses.repository.PrimaryRepository;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.m.courses.dao.specification.SpecificationUtil.buildEqualSpec;
import static org.springframework.data.jpa.domain.Specification.not;
import static org.springframework.data.jpa.domain.Specification.where;

public abstract class AbstractDao<T extends Identity<Long>> implements Dao<T, Long> {

    protected final UserAuthorizationService authorizationService;

    public AbstractDao(UserAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    protected abstract PrimaryRepository<T, Long> getRepository();

    protected boolean isAdmin() {
        return authorizationService.isAdmin();
    }

    protected Specification<T> buildReadOnlySpec() {
        if ( !isAdmin() ) {
            return not(buildEqualSpec("role", Role.ADMIN));
        }
        return where( null );
    }

    @Override
    public List<T> getAll() {
        return getRepository().findAll( buildReadOnlySpec() );
    }

    @Override
    public T get(Long id) {
        return getRepository().findOne( buildReadOnlySpec().and( buildEqualSpec("id", id)) )
                .orElse(null);
    }

    @Override
    public T create(T entity) {
        if ( isAdmin() ) {
            return getRepository().save(entity);
        }
        throw new AccessDeniedException();
    }

    @Override
    public T update(T entity) {

        if ( !canModify( entity.getId() ) ) {
            throw new AccessDeniedException();
        }

        return updateEntity(entity);
    }

    protected T updateEntity(T entity) {
        Optional<T> fromDB = getRepository().findOne(
                buildEqualSpec("id", entity.getId()) );

        if (fromDB.isEmpty()) {
            return null;
        }
        return getRepository().save(entity);
    }

    @Override
    public void delete(Long id) {
        if ( !canModify(id) ) {
            throw new AccessDeniedException();
        }
        getRepository().deleteById(id);
    }

    protected boolean canModify(Long id) {
        return isAdmin();
    }
}
