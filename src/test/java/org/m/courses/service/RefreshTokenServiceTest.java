package org.m.courses.service;

import org.hibernate.boot.spi.AbstractDelegatingMetadataBuilderImplementor;
import org.junit.jupiter.api.Test;
import org.m.courses.auth.AuthManager;
import org.m.courses.builder.RefreshTokenBuilder;
import org.m.courses.exception.TokenNotFoundException;
import org.m.courses.model.RefreshToken;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.m.courses.security.SpringUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RefreshTokenServiceTest extends AbstractServiceTest<RefreshToken> {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenBuilder refreshTokenBuilder;

    @Override
    protected AbstractService<RefreshToken> getService() {
        return refreshTokenService;
    }

    @Override
    protected RefreshToken entityToDB() {
        return refreshTokenBuilder.toDB();
    }

    @Override
    protected RefreshToken buildEntity() {
        return refreshTokenBuilder.build();
    }

    @Override
    protected RefreshToken buildNewEntity() {
        return refreshTokenBuilder.buildNew();
    }

    @Override
    protected void assertEntitiesEqual(RefreshToken e1, RefreshToken e2) {
    }

    @Override
    @Test
    void getAllEntitiesTest() {
        getService().create(buildNewEntity());

        assertEquals( 1, getService().getAll().size() );
    }

    @Test
    void createTwoRefreshPerUserTokens() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        AuthManager.loginAs( admin );

        createRefreshToken(admin);
        createRefreshToken(admin);
    }

    private void createRefreshToken(User user) {
        RefreshToken refreshToken = refreshTokenBuilder.setLogin(user.getLogin()).buildNew();
        RefreshToken createdRefreshToken = refreshTokenService.create( refreshToken );
        assertEquals( createdRefreshToken.getLogin(), user.getLogin() );
        assertNotNull( refreshTokenService.getUserByToken( createdRefreshToken.getToken() ));
        assertEquals( refreshTokenService.getUserByToken( createdRefreshToken.getToken() ).getUser(), user);
    }

    @Test
    void deleteRefreshToken() {
        RefreshToken createdRefreshToken = refreshTokenService.create( buildNewEntity() );

        refreshTokenService.delete( "ffdsf" );
        RefreshToken tokenFromDb = refreshTokenService.get( createdRefreshToken.getId() );
        assertEquals( createdRefreshToken, tokenFromDb);

        refreshTokenService.delete( createdRefreshToken.getToken() );

        assertNull(refreshTokenService.get( createdRefreshToken.getId() ));
    }

    @Test
    void getUserByToken() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        AuthManager.loginAs( admin );

        String tokenNotInDb = "asdasfsdf";
        Exception exception =
                assertThrowsExactly(TokenNotFoundException.class, () -> refreshTokenService.getUserByToken(tokenNotInDb) );
        assertEquals( exception.getMessage(), "refresh token not found with " + tokenNotInDb);

        RefreshToken refreshToken = refreshTokenBuilder.setLogin(admin.getLogin()).buildNew();
        refreshTokenService.create( refreshToken );
        SpringUser springUser = refreshTokenService.getUserByToken( refreshToken.getToken() );
        assertEquals( springUser.getUser(), admin );
    }
}
