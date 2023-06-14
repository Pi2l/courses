package org.m.courses.service;

import org.m.courses.dao.AbstractDao;
import org.m.courses.dao.RefreshTokenDao;
import org.m.courses.exception.TokenNotFoundException;
import org.m.courses.model.RefreshToken;
import org.m.courses.security.SpringUser;
import org.springframework.data.jpa.domain.Specification;
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

    public void delete(String refreshToken) {
        delete( buildEqualSpec("token", refreshToken) );
    }

    public void delete(Specification<RefreshToken> specification) {
        RefreshToken token = get( specification );
        if (token == null) {
            return;
        }
        delete( token.getId() );
    }

    public SpringUser getUserByToken(String refreshTokenStr) {
        RefreshToken refreshToken = get( buildEqualSpec("token", refreshTokenStr) );
        if (refreshToken == null) {
            throw new TokenNotFoundException("refresh token not found with " + refreshTokenStr);
        }
        return new SpringUser( userService.getDao().findByLogin( refreshToken.getLogin() ).get() );
    }

    public boolean isTokenUnique(String refreshTokenStr) {
        return get( buildEqualSpec("token", refreshTokenStr) ) == null;
    }

    public boolean isTagUnique(int tag) {
        return get( buildEqualSpec("tag", tag) ) == null;
    }
}
