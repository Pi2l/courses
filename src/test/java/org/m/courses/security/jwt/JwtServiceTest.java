package org.m.courses.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import org.m.courses.auth.AuthManager;
import org.m.courses.builder.UserBuilder;
import org.m.courses.dao.Autologinable;
import org.m.courses.model.RefreshToken;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.m.courses.service.RefreshTokenService;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class JwtServiceTest extends Autologinable {

    @Autowired
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

        String refreshTokenStr = jwtService.generateRefreshToken();

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass( RefreshToken.class );

        verify( refreshTokenService, times(1) ).create( captor.capture() ) ;
        RefreshToken refreshToken = captor.getValue();

        assertEquals( refreshToken.getToken(), refreshTokenStr );
        assertEquals( refreshToken.getLogin(), admin.getLogin() );
    }
}
