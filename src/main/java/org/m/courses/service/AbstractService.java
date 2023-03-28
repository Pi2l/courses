package org.m.courses.service;

import org.m.courses.dao.AbstractDao;
import org.m.courses.model.Identity;

import java.util.List;

public abstract class AbstractService<T extends Identity<Long>> {

    protected abstract AbstractDao<T> getDao();

    protected List<T> getAll() {
        return getDao().getAll();
    }

    protected T get(Long id) {
        return getDao().get(id);
    }

    protected T create(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity cannot be null");
        }
        return getDao().create( entity );
    }

    protected T update(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity cannot be null");
        }
        return getDao().update( entity );
    }

    protected void delete(Long id) {
        getDao().delete(id);
    }
}
