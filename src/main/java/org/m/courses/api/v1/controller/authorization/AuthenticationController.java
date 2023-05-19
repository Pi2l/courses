package org.m.courses.api.v1.controller.authorization;

import com.auth0.jwt.exceptions.TokenExpiredException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.m.courses.model.User;
import org.m.courses.security.SpringUser;
import org.m.courses.security.jwt.JwtService;
import org.m.courses.service.RefreshTokenService;
import org.springframework.http.MediaType;
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
        return buildAuthenticationResponse();
    }

    @PostMapping("/logout")
    @ResponseBody
    public void logout(@RequestParam @NotEmpty String refreshToken) {
        refreshTokenService.delete(refreshToken);
        SecurityContextHolder.getContext().setAuthentication( null );
        SecurityContextHolder.clearContext();
    }

    @PostMapping("/refresh")
    @ResponseBody
    public AuthenticationResponse refreshToken(@RequestParam @NotEmpty String refreshToken) {
        validateRefreshToken(refreshToken);
        SpringUser springUser = refreshTokenService.getUserByToken( refreshToken );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        springUser,
                        springUser.getUser().getLogin(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + springUser.getUser().getRole())) );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        refreshTokenService.delete(refreshToken);
        return buildAuthenticationResponse();
    }

    private void validateRefreshToken(String refreshToken){
        try {
            jwtService.verify(refreshToken);
        } catch ( TokenExpiredException expiredException ){
            refreshTokenService.delete( refreshToken );
            throw expiredException;
        }
    }

    private AuthenticationResponse buildAuthenticationResponse() {
        return new AuthenticationResponse(
                jwtService.generateAccessToken(),
                jwtService.generateRefreshToken(),
                jwtService.getAccessTokenExpirationInMinutes(),
                jwtService.getRefreshTokenExpirationInMinutes() );
    }

}
