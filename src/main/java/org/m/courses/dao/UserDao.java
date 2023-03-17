package org.m.courses.dao;

import org.m.courses.model.User;
import org.m.courses.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Component
public class UserDao implements Dao<User> {

    private final UserRepository repository;

    public UserDao(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<User> getAll() {
        return repository.findAll();
    }

    @Override
    public Optional<User> get(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<User> create(User obj) {
        if (obj != null && obj.getId() == null) {
            return Optional.of(repository.save(obj));
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> update(User obj) {
        if (obj != null && get(obj.getId()).isPresent()) {
            return Optional.of(repository.save(obj));
        }
        return Optional.empty();
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
