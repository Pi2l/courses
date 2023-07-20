package org.m.courses.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.m.courses.auth.AuthManager;
import org.m.courses.builder.UserBuilder;
import org.m.courses.dao.Autologinable;
import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.RefreshToken;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.m.courses.service.RefreshTokenService;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class JwtServiceTest extends Autologinable {

    @SpyBean
    private JwtService jwtService;

    @Autowired
    private UserBuilder userBuilder;

    @SpyBean
    private RefreshTokenService refreshTokenService;

    @Test
    void generateAccessToken() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        AuthManager.loginAs( admin );

        String accessTokenStr = jwtService.generateAccessToken();
        DecodedJWT accessToken = JWT.decode( accessTokenStr );
        assertEquals( accessToken.getSubject(), admin.getLogin() );
    }

    @Test
    void generateRefreshToken() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        AuthManager.loginAs( admin );

        String refreshTokenStr = jwtService.generateRefreshToken( admin.getLogin() );

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass( RefreshToken.class );

        verify( refreshTokenService, times(1) ).create( captor.capture() ) ;
        RefreshToken refreshToken = captor.getValue();

        assertEquals( refreshToken.getToken(), refreshTokenStr );
        assertEquals( refreshToken.getLogin(), admin.getLogin() );
    }

    @Test
    void generateMoreThanOneNewRefreshTokenPerUser() {
        //one user has to have possibility to generate one or more refresh "token family": https://auth0.com/blog/refresh-tokens-what-are-they-and-when-to-use-them/#Refresh-Token-Automatic-Reuse-Detection
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        AuthManager.loginAs( admin );

        String refreshTokenStr1 = jwtService.generateRefreshToken( admin.getLogin() );
        String refreshTokenStr2 = jwtService.generateRefreshToken( admin.getLogin() );

        RefreshToken refreshToken1 = refreshTokenService.get( whereTokenEqualsTo(refreshTokenStr1) );
        RefreshToken refreshToken2 = refreshTokenService.get( whereTokenEqualsTo(refreshTokenStr2) );

        assertEquals( refreshToken1.getToken(), refreshTokenStr1 );
        assertEquals( refreshToken2.getToken(), refreshTokenStr2 );

        assertNull( refreshToken1.getReplacedByToken() );
        assertNull( refreshToken2.getReplacedByToken() );

        assertNotEquals( refreshToken1.getToken(), refreshToken2.getToken() );
    }

    @Test
    void generateRefreshTokenThatReplaceItsAncestor() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        AuthManager.loginAs( admin );

        String refreshTokenStr1 = jwtService.generateRefreshToken( admin.getLogin() );
        String refreshTokenStr2 = jwtService.generateRefreshTokenSuccessor(refreshTokenStr1, admin.getLogin() );
        String refreshTokenStr3 = jwtService.generateRefreshTokenSuccessor(refreshTokenStr2, admin.getLogin() );

        RefreshToken refreshToken1 = refreshTokenService.get( whereTokenEqualsTo(refreshTokenStr1) );
        RefreshToken refreshToken2 = refreshTokenService.get( whereTokenEqualsTo(refreshTokenStr2) );
        RefreshToken refreshToken3 = refreshTokenService.get( whereTokenEqualsTo(refreshTokenStr3) );

        assertNull(refreshToken3.getReplacedByToken());
        assertEquals(refreshToken2.getReplacedByToken(), refreshToken3.getToken());
        assertEquals(refreshToken1.getReplacedByToken(), refreshToken2.getToken());

        assertFalse( refreshToken1.getIsActive() );
        assertFalse( refreshToken2.getIsActive() );
        assertTrue( refreshToken3.getIsActive() );
    }

    @Test
    void generateRefreshTokenThatIsNotAlreadyActive() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        AuthManager.loginAs( admin );
        String refreshTokenStr1 = jwtService.generateRefreshToken( admin.getLogin() );
        String refreshTokenStr2 = jwtService.generateRefreshTokenSuccessor(refreshTokenStr1, admin.getLogin() );
        String refreshTokenStr3 = jwtService.generateRefreshTokenSuccessor(refreshTokenStr2, admin.getLogin() );
        String refreshTokenStr4 = jwtService.generateRefreshTokenSuccessor(refreshTokenStr3, admin.getLogin() );

        assertThrowsExactly(AccessDeniedException.class, () -> jwtService.generateRefreshTokenSuccessor(refreshTokenStr3, admin.getLogin()) );
        AuthManager.loginAs( admin );
        assertThrowsExactly(AccessDeniedException.class, () -> jwtService.generateRefreshTokenSuccessor(refreshTokenStr4, admin.getLogin()) );

        RefreshToken refreshToken1 = refreshTokenService.get( whereTokenEqualsTo(refreshTokenStr1) );
        RefreshToken refreshToken2 = refreshTokenService.get( whereTokenEqualsTo(refreshTokenStr2) );
        RefreshToken refreshToken3 = refreshTokenService.get( whereTokenEqualsTo(refreshTokenStr3) );
        RefreshToken refreshToken4 = refreshTokenService.get( whereTokenEqualsTo(refreshTokenStr4) );

        assertNull( refreshToken1 );
        assertNull( refreshToken2 );
        assertNull( refreshToken3 );
        assertNull( refreshToken4 );
        verify( jwtService, times(1)).removeDescendantRefreshTokens( anyString() );
    }

    private Specification<RefreshToken> whereTokenEqualsTo(String refreshTokenStr) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("token"), refreshTokenStr);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void removeDescendantRefreshTokensTest(int i) {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        AuthManager.loginAs( admin );
        String refreshTokenStr1 = jwtService.generateRefreshToken( admin.getLogin() );
        String refreshTokenStr2 = jwtService.generateRefreshTokenSuccessor(refreshTokenStr1, admin.getLogin() );
        String refreshTokenStr3 = jwtService.generateRefreshTokenSuccessor(refreshTokenStr2, admin.getLogin() );
        String [] refreshTokenStrs = { refreshTokenStr1, refreshTokenStr2, refreshTokenStr3 };

        jwtService.removeDescendantRefreshTokens( refreshTokenStrs[i] ); // any of refreshTokenStr

        Arrays.stream(refreshTokenStrs)
                .forEach(refreshToken -> assertNull( refreshTokenService.get( whereTokenEqualsTo(refreshToken) ) ) );
    }
}
