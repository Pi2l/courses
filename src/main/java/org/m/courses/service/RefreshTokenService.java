package org.m.courses.service;

import org.m.courses.dao.AbstractDao;
import org.m.courses.dao.RefreshTokenDao;
import org.m.courses.model.RefreshToken;
import org.m.courses.security.SpringUser;
import org.springframework.stereotype.Service;

import static org.m.courses.filtering.specification.SpecificationUtil.buildEqualSpec;

@Service
public class RefreshTokenService extends AbstractService<RefreshToken> {

    private final RefreshTokenDao tokenDao;
    private final UserService userService;
    private final UserAuthorizationService authorizationService;

    public RefreshTokenService(RefreshTokenDao tokenDao, UserService userService, UserAuthorizationService authorizationService) {
        this.tokenDao = tokenDao;
        this.userService = userService;
        this.authorizationService = authorizationService;
    }

    @Override
    protected AbstractDao<RefreshToken> getDao() {
        return tokenDao;
    }

    @Override
    public RefreshToken create(RefreshToken entity) {
        entity.setLogin( authorizationService.getCurrentUser().getLogin() );
        return super.create( entity );
    }

    public void delete(String refreshToken) {
        RefreshToken token = get( buildEqualSpec("token", refreshToken) );
        if (token == null) {
            return;
        }
        delete( token.getId() );
    }

    public SpringUser getUserByToken(String refreshTokenStr) {
        RefreshToken refreshToken = get( buildEqualSpec("token", refreshTokenStr) );
        if (refreshToken == null) {
            throw new IllegalArgumentException("refresh token not found with " + refreshTokenStr);
        }
        return new SpringUser( userService.getDao().findByLogin( refreshToken.getLogin() ).get() );
    }
}
