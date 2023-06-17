package org.m.courses.api.v1.controller.authorization;

import com.auth0.jwt.exceptions.TokenExpiredException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.m.courses.security.SpringUser;
import org.m.courses.security.jwt.JwtService;
import org.m.courses.service.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;

import static org.m.courses.api.v1.controller.common.ApiPath.API;


@RestController
@RequestMapping(API)
@Tag(name = "Authorization", description = "The Authorization API")
public class AuthenticationController {

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
    public AuthenticationResponse authenticationRequest(@RequestBody AuthenticationRequest request) {
        String login = request.getLogin();
        String password = request.getPassword();

        Authentication authentication = authenticationManager
                .authenticate( new UsernamePasswordAuthenticationToken( login, password ) );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return buildAuthenticationResponse( jwtService.generateRefreshToken(login) );
    }

    @PostMapping("/logout")
    @ResponseBody
    public void logout(@RequestParam @NotEmpty String refreshToken) {
        jwtService.removeDescendantRefreshTokens(refreshToken);
        SecurityContextHolder.getContext().setAuthentication( null );
        SecurityContextHolder.clearContext();
    }

    @PostMapping("/refresh")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public AuthenticationResponse refreshToken(@RequestParam @NotEmpty String refreshTokenStr) {//401, 403
        SpringUser springUser = refreshTokenService.getUserByToken( refreshTokenStr );// nullPointer when user changed its login and tries to refresh token with old login

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        springUser,
                        springUser.getUser().getLogin(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + springUser.getUser().getRole())) );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        validateRefreshToken(refreshTokenStr);

        String newRefreshToken = jwtService.generateRefreshTokenSuccessor( refreshTokenStr, springUser.getUser().getLogin() );
        return buildAuthenticationResponse( newRefreshToken );
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

    private AuthenticationResponse buildAuthenticationResponse(String refreshToken) {
        return new AuthenticationResponse(
                jwtService.generateAccessToken(),
                refreshToken,
                jwtService.getAccessTokenExpirationInMinutes(),
                jwtService.getRefreshTokenExpirationInMinutes() );
    }

}
