package org.m.courses.builder;

import org.m.courses.dao.RefreshTokenDao;
import org.m.courses.model.RefreshToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;


@Component
public class RefreshTokenBuilder {

    @Autowired private RefreshTokenDao refreshTokenDao;

    RefreshToken refreshToken;

    public static RefreshTokenBuilder builder() {
        return new RefreshTokenBuilder();
    }

    public RefreshTokenBuilder() {
        initDefaultUser();
    }

    public RefreshToken build() {
        RefreshToken odlEntity = refreshToken;
        initDefaultUser();
        return odlEntity;
    }

    public RefreshToken buildNew() {
        return setId(null)
                .build();
    }

    // Spring based
    public RefreshToken toDB() {
        return refreshTokenDao.create( buildNew() );
    }

    private RefreshTokenBuilder initDefaultUser() {
        long randomValue = Math.abs(new SecureRandom().nextLong()) % 1000000;
        this.refreshToken = new RefreshToken();

        setId( randomValue );
        setToken( "Token_" + randomValue );
        setLogin( "Login_" + randomValue );
        setReplacedBy(null);
        setIsActive(true);
        return this;
    }

    public RefreshTokenBuilder setId(Long id) {
        refreshToken.setId(id);
        return this;
    }

    public RefreshTokenBuilder setToken(String name) {
        refreshToken.setToken(name);
        return this;
    }

    public RefreshTokenBuilder setLogin(String login) {
        refreshToken.setLogin(login);
        return this;
    }

    public RefreshTokenBuilder setReplacedBy(String replacedBy) {
        refreshToken.setReplacedByToken(replacedBy);
        return this;
    }

    public RefreshTokenBuilder setIsActive(boolean isActive) {
        refreshToken.setIsActive(isActive);
        return this;
    }
}
