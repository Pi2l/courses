package org.m.courses.service;

import org.m.courses.dao.AbstractDao;
import org.m.courses.model.Identity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public abstract class AbstractService<T extends Identity<Long>> {

    protected abstract AbstractDao<T> getDao();

    public List<T> getAll() {
        return getDao().getAll();
    }

    public Page<T> getAll(Pageable pageable, Specification<T> filter) {
        return getDao().getAll( pageable, filter );
    }

    public T get(Long id) {
        return getDao().get(id);
    }

    public T create(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity cannot be null");
        }
        return getDao().create( entity );
    }

    public T update(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity cannot be null");
        }
        return getDao().update( entity );
    }

    public void delete(Long id) {
        getDao().delete(id);
    }
}
