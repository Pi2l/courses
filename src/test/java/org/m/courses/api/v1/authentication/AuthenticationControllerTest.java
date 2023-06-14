package org.m.courses.api.v1.authentication;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.m.courses.api.v1.controller.authorization.AuthenticationController;
import org.m.courses.api.v1.controller.authorization.AuthenticationResponse;
import org.m.courses.builder.RefreshTokenBuilder;
import org.m.courses.builder.UserBuilder;
import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.RefreshToken;
import org.m.courses.security.SpringUser;
import org.m.courses.security.jwt.JwtService;
import org.m.courses.service.RefreshTokenService;
import org.m.courses.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.m.courses.api.v1.controller.common.ApiPath.API;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest( AuthenticationController.class )
public class AuthenticationControllerTest {

    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private String getJson(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString( object );
    }

    @Test
    public void loginWithWrongCredentialsTest() throws Exception {
        given( authenticationManager.authenticate( any() ) ).willThrow( BadCredentialsException.class );

        mockMvc.perform( post( API + "/login" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( "{\"login\": \"login1\", \"password\": \"password1\"}" ) )
                .andExpect( status().isUnauthorized() );
    }

    @Test
    public void loginTest() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken("login1", "password1");
        when( authenticationManager.authenticate( any() ) ).thenReturn( authentication );

        mockMvc.perform( post( API + "/login" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( "{\"login\": \"login1\", \"password\": \"password1\"}" ) )
                .andExpect( status().isOk() );

        verify( authenticationManager, times(1) ).authenticate( authentication );
        verify( jwtService, times(1) ).generateAccessToken();
        verify( jwtService, times(1) ).generateRefreshToken("login1");
        verify( jwtService, times(1) ).getAccessTokenExpirationInMinutes();
        verify( jwtService, times(1) ).getRefreshTokenExpirationInMinutes();
    }

    @Test
    public void loginReturnTokensTest() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken("login1", "password1");
        when( authenticationManager.authenticate( any() ) ).thenReturn( authentication );

        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        AuthenticationResponse response = mockJwtGenerate(accessToken, refreshToken, "login1");

        mockMvc.perform( post( API + "/login" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( "{\"login\": \"login1\", \"password\": \"password1\"}" ) )
                .andDo(print())
                .andExpect( content().json( getJson(response) ) )
                .andExpect( status().isOk() );

        verify( authenticationManager, times(1) ).authenticate( any() );
    }

    @Test
    public void refreshTokenNotFoundTest() throws Exception {
        String refreshToken = "refreshToken1";
        when(refreshTokenService.getUserByToken(refreshToken) ).thenThrow( new IllegalArgumentException("refresh token not found with " + refreshToken) );

        mockMvc.perform( post( API + "/refresh" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .param( "refreshTokenStr", refreshToken) )
                .andDo( print() )
                .andExpect( jsonPath("$.cause").value( "refresh token not found with " + refreshToken ) )
                .andExpect( status().isBadRequest() );

        verify( authenticationManager, never() ).authenticate( any() );
        verify( refreshTokenService, never() ).delete( refreshToken );
    }

    @Test
    public void getNewRefreshTokenTest() throws Exception {
        String refreshToken = "refreshToken1";
        RefreshToken newRefreshToken = RefreshTokenBuilder.builder().build();

        SpringUser springUser = new SpringUser( UserBuilder.builder().build() );
        when( refreshTokenService.getUserByToken(refreshToken) )
                .thenReturn( springUser );
        when( jwtService.generateRefreshToken(refreshToken, springUser.getUser().getLogin()) ).thenReturn( newRefreshToken.getToken() );

        String newAccessToken = "newAccessToken";
        String newRefreshTokenStr = newRefreshToken.getToken();
        AuthenticationResponse response = mockJwtGenerate(newAccessToken, newRefreshTokenStr, springUser.getUser().getLogin(), newRefreshTokenStr);

        mockMvc.perform( post( API + "/refresh" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .param( "refreshTokenStr", refreshToken) )
                .andExpect( content().json( getJson(response) ) )
                .andExpect( status().isOk() );
    }

    @Test
    public void getRefreshTokenWithNotActiveRefreshTokenTest() throws Exception {
        String refreshToken = "NotActiveRefreshToken";

        SpringUser springUser = new SpringUser( UserBuilder.builder().build() );
        when( refreshTokenService.getUserByToken(refreshToken) )
                .thenReturn( springUser );
        when( jwtService.generateRefreshToken(refreshToken, springUser.getUser().getLogin()) ).thenThrow( AccessDeniedException.class );

        mockMvc.perform( post( API + "/refresh" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .param( "refreshTokenStr", refreshToken) )
                .andExpect( status().isForbidden() );
    }

    @Test
    public void getExpiredRefreshTokenTest() throws Exception {
        String refreshToken = "ExpiredRefreshToken";
        RefreshToken newRefreshToken = RefreshTokenBuilder.builder().build();

        SpringUser springUser = new SpringUser( UserBuilder.builder().build() );
        when( refreshTokenService.getUserByToken(refreshToken) )
                .thenReturn( springUser );
        doThrow(TokenExpiredException.class).when( jwtService ).verify(refreshToken);

        mockMvc.perform( post( API + "/refresh" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .param( "refreshTokenStr", refreshToken) )
                .andExpect( status().isUnauthorized() );
    }

    @Test
    public void logoutTest() throws Exception {
        String refreshToken = "refreshToken1";
        Authentication authentication = new UsernamePasswordAuthenticationToken("login1", "password1");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform( post( API + "/logout" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .param( "refreshToken", refreshToken) )
                .andExpect( status().isOk() );

        verify( jwtService, times(1) ).removeDescendantRefreshTokens( refreshToken );
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private AuthenticationResponse mockJwtGenerate(String accessToken, String refreshToken, String login) {
        when( jwtService.generateRefreshToken(login) ).thenReturn(refreshToken);
        return mockJwtGenerate(accessToken, refreshToken);
    }

    private AuthenticationResponse mockJwtGenerate(String accessToken, String refreshToken, String login, String replacedBy) {
        when( jwtService.generateRefreshToken(eq(login), anyInt()) ).thenReturn(refreshToken);
        return mockJwtGenerate(accessToken, refreshToken);
    }

    private AuthenticationResponse mockJwtGenerate(String accessToken, String refreshToken) {
        when( jwtService.generateAccessToken()).thenReturn(accessToken);
        when( jwtService.getAccessTokenExpirationInMinutes()).thenReturn( 10 );
        when( jwtService.getRefreshTokenExpirationInMinutes()).thenReturn( 60 );
        return new AuthenticationResponse( accessToken, refreshToken, 10, 60);
    }

}
