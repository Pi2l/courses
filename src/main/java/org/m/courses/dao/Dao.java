package org.m.courses.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {

    List<T> getAll();

    Optional<T> get(Long id);
    Optional<T> create(T obj);

    Optional<T> update(T obj);

    void delete(Long id);
}
