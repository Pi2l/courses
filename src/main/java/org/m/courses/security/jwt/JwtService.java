package org.m.courses.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.m.courses.exception.AccessDeniedException;
import org.m.courses.exception.TokenNotFoundException;
import org.m.courses.model.RefreshToken;
import org.m.courses.security.UserDetailsServiceImpl;
import org.m.courses.service.RefreshTokenService;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.UUID;

import static org.m.courses.filtering.specification.SpecificationUtil.buildEqualSpec;

@Service
public class JwtService {

    @Value("${org.m.jwt.jwtKey}")
    private String jwtKey;

    @Value("${org.m.jwt.accessTokenExpirationInSeconds}")
    private Integer accessTokenExpirationInSeconds;

    @Value("${org.m.jwt.refreshTokenExpirationInSeconds}")
    private Integer refreshTokenExpirationInSeconds;

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
                getExpirationDate(accessTokenExpirationInSeconds) );
    }

    public String generateRefreshToken(String login) {
        int tag;
        do {
            tag = UUID.randomUUID().hashCode();
        } while ( !refreshTokenService.isTagUnique(tag) );
        return generateRefreshToken( login, tag );
    }

    public String generateRefreshToken(String login, int tag) {
        Date expirationDate = getExpirationDate(refreshTokenExpirationInSeconds);
        String refreshTokenStr;
        do {
            refreshTokenStr = generateToken(UUID.randomUUID().toString(), expirationDate);
        } while ( !refreshTokenService.isTokenUnique(refreshTokenStr) );

        RefreshToken refreshToken = new RefreshToken( );
        refreshToken.setToken( refreshTokenStr );
        refreshToken.setLogin( login );
        refreshToken.setIsActive( true );
        refreshToken.setReplacedByToken( null );
        refreshToken.setTag( tag );
        refreshTokenService.create( refreshToken );

        return refreshTokenStr;
    }

    private String generateToken( String subject, Date expirationDate ) {
        return JWT.create()
                .withIssuer( "Web" )
                .withSubject( subject )
                .withIssuedAt( getCurrentDate() )
                .withExpiresAt( expirationDate )
                .sign( algorithm );
    }

    private Date getCurrentDate() {
        return new Date( System.currentTimeMillis() );
    }

    private Date getExpirationDate( int expirationInSeconds ) {
        return new Date( System.currentTimeMillis() + expirationInSeconds * 1000L );
    }

    public void verify( String jwt ) {
        JWTVerifier verifier = JWT.require( algorithm ).build();
        verifier.verify(jwt);
    }

    public UserDetails getUserDetailsByJwt( String jwtKey ) {
        String login = getLogin( jwtKey );
        if ( refreshTokenService.getAll(Pageable.unpaged(), buildEqualSpec("login", login) ).getSize() == 0) {
            login = null; // in order userDetailsService.loadUserByUsername throw UsernameNotFoundException
        }
        return userDetailsService.loadUserByUsername( login );
    }

    public String getLogin(String jwt) {
        DecodedJWT decodedJWT = JWT.decode( jwt );
        return decodedJWT.getSubject();
    }

    public Integer getAccessTokenExpirationInSeconds() {
        return accessTokenExpirationInSeconds;
    }

    public Integer getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpirationInSeconds;
    }

    public String generateRefreshTokenSuccessor(String refreshTokenStr, String login) {
        RefreshToken refreshToken = refreshTokenService.get( getTokenSpec(refreshTokenStr) );
        if ( refreshToken == null ) {
            throw new AccessDeniedException();
        }

        if ( !refreshToken.getIsActive() ) {
            revokeDescendantRefreshTokens(refreshToken);
            removeDescendantRefreshTokens(refreshTokenStr);
            SecurityContextHolder.getContext().setAuthentication(null);
            throw new AccessDeniedException();
        }

        return rotateRefreshToken(refreshToken, login);
    }

    private String rotateRefreshToken(RefreshToken activeRefreshToken, String login) {
        String newRefreshToken = this.generateRefreshToken(login, activeRefreshToken.getTag() );
        revokeRefreshToken(activeRefreshToken, newRefreshToken);
        return newRefreshToken;
    }

    private void revokeRefreshToken(RefreshToken activeRefreshToken, String newRefreshToken) {
        activeRefreshToken.setReplacedByToken(newRefreshToken);
        activeRefreshToken.setIsActive( false );
        refreshTokenService.update(activeRefreshToken);
    }

    private void revokeDescendantRefreshTokens(RefreshToken refreshToken) {
        String replacedByToken = refreshToken.getReplacedByToken();

        if ( replacedByToken == null || replacedByToken.isBlank() ) {
            refreshToken.setIsActive(false);
            refreshTokenService.update( refreshToken );
            return;
        }

        RefreshToken childRefreshToken = refreshTokenService.get( getTokenSpec( replacedByToken ) );
        revokeDescendantRefreshTokens( childRefreshToken );
    }

    private Specification<RefreshToken> getTokenSpec(String refreshToken) {
        return (root, query, criteriaBuilder) -> getTokenPredicate( root, query, criteriaBuilder, refreshToken );
    }

    private Predicate getTokenPredicate(Root<RefreshToken> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, String refreshToken) {
        return criteriaBuilder.equal( root.get("token"), refreshToken );
    }

    public void removeDescendantRefreshTokens(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService.get( getTokenSpec(refreshTokenStr) );
        if (refreshToken == null) {
            throw new TokenNotFoundException("");
        }

        refreshTokenService.delete( whereTagEquals(refreshToken.getTag()) );
    }

    private Specification<RefreshToken> whereTagEquals(Integer tag) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal( root.get("tag"), tag );
    }
}
