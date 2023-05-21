package org.m.courses.service;

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

        RefreshToken createdRefreshToken = refreshTokenService.create( buildNewEntity() );
        assertEquals( createdRefreshToken.getLogin(), admin.getLogin() );
        assertNotNull( refreshTokenService.getUserByToken( createdRefreshToken.getToken() ));

        RefreshToken createdRefreshToken2 = refreshTokenService.create( buildNewEntity() );
        assertEquals( createdRefreshToken2.getLogin(), admin.getLogin() );

        Exception exception = assertThrowsExactly(TokenNotFoundException.class,
                () -> refreshTokenService.getUserByToken( createdRefreshToken.getToken() ) );
        assertEquals( exception.getMessage(), "refresh token not found with " + createdRefreshToken.getToken() );
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
        RefreshToken createdRefreshToken = refreshTokenService.create( buildNewEntity() );

        String tokenNotInDb = "asdasfsdf";
        Exception exception =
                assertThrowsExactly(TokenNotFoundException.class, () -> refreshTokenService.getUserByToken(tokenNotInDb) );
        assertEquals( exception.getMessage(), "refresh token not found with " + tokenNotInDb);

        SpringUser springUser = refreshTokenService.getUserByToken( createdRefreshToken.getToken() );
        assertEquals( springUser.getUser(), admin );
    }
}
