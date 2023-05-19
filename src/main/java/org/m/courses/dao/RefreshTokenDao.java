package org.m.courses.dao;

import org.m.courses.model.RefreshToken;
import org.m.courses.model.User;
import org.m.courses.repository.PrimaryRepository;
import org.m.courses.repository.RefreshTokenRepository;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.stereotype.Component;

import static org.m.courses.filtering.specification.SpecificationUtil.buildEqualSpec;

@Component
public class RefreshTokenDao extends AbstractDao<RefreshToken> {

    private final RefreshTokenRepository repository;
    private final UserDao userDao;

    public RefreshTokenDao(RefreshTokenRepository repository, UserAuthorizationService authorizationService, UserDao userDao) {
        super(authorizationService);
        this.repository = repository;
        this.userDao = userDao;
    }

    @Override
    public RefreshToken create(RefreshToken entity) {
        return getRepository().save(entity);
    }

    @Override
    protected boolean canModify(Long id) {
        RefreshToken refreshToken = get( id );
        if (refreshToken == null) {
            return isAdmin();
        }

        User user = userDao.get(buildEqualSpec("login", refreshToken.getLogin()));
        return isAdmin() || authorizationService.getCurrentUser().equals( user );
    }

    @Override
    protected PrimaryRepository<RefreshToken, Long> getRepository() {
        return repository;
    }
}
