package org.m.courses.dao;

import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.Identity;
import org.m.courses.repository.PrimaryRepository;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.m.courses.dao.specification.SpecificationUtil.buildEqualSpec;

public abstract class AbstractDao<T extends Identity<Long>> {

    protected final UserAuthorizationService authorizationService;

    public AbstractDao(UserAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    protected abstract PrimaryRepository<T, Long> getRepository();

    protected boolean isAdmin() {
        return authorizationService.isAdmin();
    }

    public List<T> getAll() {
        return getRepository().findAll();
    }

    public Page<T> getAll(Pageable pageable) {
        return getRepository().findAll( pageable );
    }

    public T get(Long id) {
        return getRepository().findOne( buildEqualSpec("id", id) )
                .orElse(null);
    }

    public T create(T entity) {
        if ( isAdmin() ) {
            return getRepository().save(entity);
        }
        throw new AccessDeniedException();
    }

    public T update(T entity) {

        if ( !canModify( entity.getId() ) ) {
            throw new AccessDeniedException();
        }

        return getRepository().save(entity);
    }

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
