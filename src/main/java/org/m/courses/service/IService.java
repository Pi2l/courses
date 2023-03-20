package org.m.courses.service;

import java.util.List;

public interface IService<T> {

    T get(Long id);

    List<T> getAll();

    T create(T instance);

    T update(T instance);

    void delete(Long id);
}
