package org.m.courses.dao;

import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.m.courses.repository.PrimaryRepository;
import org.m.courses.repository.UserRepository;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.m.courses.dao.specification.SpecificationUtil.buildEqualSpec;
import static org.springframework.data.jpa.domain.Specification.not;
import static org.springframework.data.jpa.domain.Specification.where;

@Component
public class UserDao extends AbstractDao<User> {

    private final UserRepository repository;

    public UserDao(UserRepository repository, UserAuthorizationService authorizationService) {
        super(authorizationService);
        this.repository = repository;
    }

    @Override
    public List<User> getAll() {
        return getRepository().findAll( buildReadOnlySpec() );
    }

    public User get(Long id) {
        Specification<User> equalIdSpec = buildEqualSpec("id", id);

        return getRepository().findOne( equalIdSpec.and( buildReadOnlySpec() ) )
                .orElse(null);
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

    private Specification<User> buildReadOnlySpec() {
        if ( !isAdmin() ) {
            return not(buildEqualSpec("role", Role.ADMIN));
        }
        return where( null );
    }
}
