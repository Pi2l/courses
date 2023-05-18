package org.m.courses.dao;

import org.m.courses.model.RefreshToken;
import org.m.courses.repository.PrimaryRepository;
import org.m.courses.repository.RefreshTokenRepository;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenDao extends AbstractDao<RefreshToken> {

    private final RefreshTokenRepository repository;

    public RefreshTokenDao(RefreshTokenRepository repository, UserAuthorizationService authorizationService) {
        super(authorizationService);
        this.repository = repository;
    }

    @Override
    protected PrimaryRepository<RefreshToken, Long> getRepository() {
        return repository;
    }
}
