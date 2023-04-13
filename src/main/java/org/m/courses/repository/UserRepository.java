package org.m.courses.repository;

import org.m.courses.model.User;

import java.util.Optional;


public interface UserRepository extends PrimaryRepository<User, Long> {

    Optional<User> findByLogin(String login);
}
