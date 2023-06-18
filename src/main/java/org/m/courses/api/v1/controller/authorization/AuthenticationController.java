package org.m.courses.api.v1.controller.authorization;

import com.auth0.jwt.exceptions.TokenExpiredException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.m.courses.security.SpringUser;
import org.m.courses.security.jwt.JwtService;
import org.m.courses.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.m.courses.api.v1.controller.common.ApiPath.API;


@RestController
@RequestMapping(API)
@Tag(name = "Authorization", description = "The Authorization API")
public class AuthenticationController {

    @Value("${org.m.cookie.refreshJwtAgeInSeconds}")
    private int MAX_COOKIE_AGE;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthenticationController(AuthenticationManager authenticationManager, RefreshTokenService conversionService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = conversionService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @ResponseBody
    public AuthenticationResponse authenticationRequest(@RequestBody AuthenticationRequest request, HttpServletResponse response) {
        String login = request.getLogin();
        String password = request.getPassword();

        Authentication authentication = authenticationManager
                .authenticate( new UsernamePasswordAuthenticationToken( login, password ) );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return buildAuthenticationResponse( jwtService.generateRefreshToken(login), response );
    }

    @PostMapping("/logout")
    @ResponseBody
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookies(request);
        jwtService.removeDescendantRefreshTokens(refreshToken);

        setRefreshTokenCookie(null, 0, response);

        SecurityContextHolder.getContext().setAuthentication( null );
        SecurityContextHolder.clearContext();
    }

    @PostMapping("/refresh")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {//401, 403
        String refreshTokenStr = getRefreshTokenFromCookies(request);
        SpringUser springUser = refreshTokenService.getUserByToken( refreshTokenStr );// nullPointer when user changed its login and tries to refresh token with old login

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        springUser,
                        springUser.getUser().getLogin(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + springUser.getUser().getRole())) );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        validateRefreshToken(refreshTokenStr);

        String newRefreshToken = jwtService.generateRefreshTokenSuccessor( refreshTokenStr, springUser.getUser().getLogin() );
        return buildAuthenticationResponse( newRefreshToken, response );
    }

    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie [] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void validateRefreshToken(String refreshToken) {
        try {
            jwtService.verify(refreshToken);
        } catch ( TokenExpiredException expiredException ) {
            jwtService.removeDescendantRefreshTokens(refreshToken);
            SecurityContextHolder.getContext().setAuthentication( null );
            throw expiredException;
        }
    }

    private AuthenticationResponse buildAuthenticationResponse(String refreshToken, HttpServletResponse response) {
        setRefreshTokenCookie(refreshToken, MAX_COOKIE_AGE, response);

        return new AuthenticationResponse(
                jwtService.generateAccessToken(),
                jwtService.getAccessTokenExpirationInMinutes(),
                jwtService.getRefreshTokenExpirationInMinutes() );
    }

    private void setRefreshTokenCookie(String refreshToken, int cookieAge, HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(cookieAge);
        response.addCookie(cookie);
    }

}
