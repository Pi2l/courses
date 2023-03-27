package org.m.courses.dao;

import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.User;
import org.m.courses.repository.PrimaryRepository;
import org.m.courses.repository.UserRepository;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserDao extends AbstractDao<User> {

    private final UserRepository repository;

    public UserDao(UserRepository repository, UserAuthorizationService authorizationService) {
        super(authorizationService);
        this.repository = repository;
    }

    @Override
    public User update(User user) {
        if ( !canModify( user.getId() ) ) {
            throw new AccessDeniedException();
        }

        return getRepository().save(user);
    }

    @Override
    public void delete(Long id) {
        if ( !canModify( id ) ) {
            throw new AccessDeniedException();
        }
        repository.deleteById(id);
    }

    public Optional<User> findByLogin(String login) {
        return repository.findByLogin(login);
    }

    @Override
    protected PrimaryRepository<User, Long> getRepository() {
        return repository;
    }

    @Override
    protected boolean canModify(Long id) {
        return super.canModify(id) || authorizationService.getCurrentUser().getId().equals( id );
    }
}
