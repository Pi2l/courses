package org.m.courses.dao;

import java.util.Optional;

public interface Dao<T> {

    Iterable<T> getAll();

    Optional<T> get(Long id);

    void create(T obj);

    void update(T obj);

    void delete(Long id);
}
