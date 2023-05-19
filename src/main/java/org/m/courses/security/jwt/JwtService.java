package org.m.courses.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.m.courses.model.RefreshToken;
import org.m.courses.security.UserDetailsServiceImpl;
import org.m.courses.service.RefreshTokenService;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.UUID;

import static org.m.courses.filtering.specification.SpecificationUtil.buildEqualSpec;

@Service
public class JwtService {

    @Value("${org.m.jwt.jwtKey}")
    private String jwtKey;

    @Value("${org.m.jwt.accessTokenExpirationInMinutes}")
    private Integer accessTokenExpirationInMinutes;

    @Value("${org.m.jwt.refreshTokenExpirationInMinutes}")
    private Integer refreshTokenExpirationInMinutes;

    private Algorithm algorithm;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserAuthorizationService userAuthorizationService;

    public JwtService(RefreshTokenService refreshTokenService, UserDetailsServiceImpl userDetailsService, UserAuthorizationService userAuthorizationService) {
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
        this.userAuthorizationService = userAuthorizationService;
    }

    @PostConstruct
    public void init() throws IllegalArgumentException {
        algorithm = Algorithm.HMAC256( jwtKey );
    }

    public String generateAccessToken() {
        return generateToken( userAuthorizationService.getCurrentUser().getLogin(),
                expirationDate( accessTokenExpirationInMinutes ) );
    }

    public String generateRefreshToken() {
        Date expirationDate = expirationDate( refreshTokenExpirationInMinutes );
        String refreshTokenStr = generateToken( UUID.randomUUID().toString(), expirationDate );

        RefreshToken refreshToken = new RefreshToken( );
        refreshToken.setToken( refreshTokenStr );
        refreshTokenService.create( refreshToken );

        return refreshTokenStr;
    }

    private String generateToken( String subject, Date expirationDate ) {
        return JWT.create()
                .withIssuer( "Web" )
                .withSubject( subject )
                .withIssuedAt( currentDate() )
                .withExpiresAt( expirationDate )
                .sign( algorithm );
    }

    private Date currentDate() {
        return new Date( System.currentTimeMillis() );
    }

    private Date expirationDate( int expirationInMinutes ) {
        return new Date( System.currentTimeMillis() + expirationInMinutes * 1000L * 60 );
    }

    public void verify( String jwt ) {
        JWTVerifier verifier = JWT.require( algorithm ).build();
        verifier.verify(jwt);
    }

    public UserDetails getUserDetailsByJwt( String jwtKey ) {
        String login = getLogin( jwtKey );
        if ( refreshTokenService.get( buildEqualSpec("login", login) ) == null) {
            login = null; // in order userDetailsService.loadUserByUsername throw UsernameNotFoundException
        }
        return userDetailsService.loadUserByUsername( login );
    }

    public String getLogin(String jwt) {
        DecodedJWT decodedJWT = JWT.decode( jwt );
        return decodedJWT.getSubject();
    }

    public Integer getAccessTokenExpirationInMinutes() {
        return accessTokenExpirationInMinutes;
    }

    public Integer getRefreshTokenExpirationInMinutes() {
        return refreshTokenExpirationInMinutes;
    }
}
