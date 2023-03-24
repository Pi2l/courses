package org.m.courses.dao;

import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.m.courses.repository.UserRepository;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.jpa.domain.Specification.*;


@Component
public class UserDao implements Dao<User> {

    private final UserRepository repository;

    private final UserAuthorizationService authorizationService;

    public UserDao(UserRepository repository, UserAuthorizationService authorizationService) {
        this.repository = repository;
        this.authorizationService = authorizationService;
    }

    @Override
    public List<User> getAll() {
        return repository.findAll( buildReadOnlySpec() );
    }

    @Override
    public User get(Long id) {
        return repository.findOne( buildEqualSpec( "id", id ).and( buildReadOnlySpec() ) )
                .orElseThrow(AccessDeniedException::new);
    }

    @Override
    public User create(User user) {
        if ( authorizationService.isAdmin() ) {
            return repository.save(user);
        }
        throw new AccessDeniedException();
    }

    @Override
    public User update(User user) {
        Optional<User> userFromDB = repository.findOne(
                buildEqualSpec("id", user.getId()).and( buildWriteSpec() ));

        if (userFromDB.isEmpty()) {
            throw new AccessDeniedException();
        }
        return repository.save(user);
    }

    @Override
    public void delete(Long id) {
        Optional<User> userFromDB = repository.findOne(
                buildEqualSpec("id", id).and( buildWriteSpec() ));

        if (userFromDB.isEmpty()) {
            throw new AccessDeniedException();
        }
        repository.deleteById(id);
    }

    public Optional<User> findByLogin(String login) {
        return repository.findByLogin(login);
    }

    private Specification<User> buildEqualSpec(String fieldName, Object value) {
        return (root, cq, cb) -> cb.equal( root.get( fieldName ), value );
    }

    private Specification<User> buildReadOnlySpec() {
        if (authorizationService.isAdmin()) {
            return null;
        }
        return not(buildEqualSpec("role", Role.ADMIN));
    }

    private Specification<User> buildWriteSpec() {
        if ( authorizationService.isAdmin() ) {
            return null;
        }

        return buildEqualSpec("id", authorizationService.getCurrentUser().getId());
    }
}
