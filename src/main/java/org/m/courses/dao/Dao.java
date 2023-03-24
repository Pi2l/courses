package org.m.courses.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<T, IdType> {

    List<T> getAll();

    T get(IdType id);
    T create(T obj);

    T update(T obj);

    void delete(IdType id);
}
