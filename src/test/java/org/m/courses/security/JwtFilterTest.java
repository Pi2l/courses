package org.m.courses.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.m.courses.api.v1.controller.authorization.AuthenticationController;
import org.m.courses.api.v1.controller.authorization.AuthenticationResponse;
import org.m.courses.builder.UserBuilder;
import org.m.courses.model.User;
import org.m.courses.security.jwt.JwtAuthenticationFilter;
import org.m.courses.security.jwt.JwtService;
import org.m.courses.service.RefreshTokenService;
import org.m.courses.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.m.courses.api.v1.controller.common.ApiPath.API;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest( AuthenticationController.class )
public class JwtFilterTest {
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
    HttpServletRequest request;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    private void init() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter( new JwtAuthenticationFilter("/api/**", new String[] { "/api/login" }, jwtService) )
                .build();
    }

    private String getJson(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString( object );
    }

    @Test
    public void filterExcludedPathTest() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken("login1", "password1");
        when( authenticationManager.authenticate( any() ) ).thenReturn( authentication );

        mockMvc.perform( post( API + "/login" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( "{\"login\": \"login1\", \"password\": \"password1\"}" ) )
                .andExpect( status().isOk() );


        verify( request, never() ).getHeader( AUTHORIZATION );
    }

    @Test
    public void filterNoBearerHeaderTest() throws Exception {
        String refreshToken = "refreshToken1";
        Authentication authentication = new UsernamePasswordAuthenticationToken("login1", "password1");
        when( authenticationManager.authenticate( any() ) ).thenReturn( authentication );

        mockMvc.perform( post( API + "/logout" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION, "Basic")
                        .param( "refreshToken", refreshToken) )
                .andDo(print())
                .andExpect( status().isOk() );

        assertNull( SecurityContextHolder.getContext().getAuthentication() );
        verify( jwtService, never() ).verify( any() );
    }

    @Test
    public void filterVerifyJwtAndAuthorizeTest() throws Exception {
        String refreshToken = "refreshToken1";
        User user = UserBuilder.builder().build();
        SpringUser springUser = new SpringUser( user );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                springUser,
                springUser.getUser().getLogin(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );

        when( jwtService.getUserDetailsByJwt(refreshToken) ).thenReturn( springUser );
        when( jwtService.getLogin( any() ) ).thenReturn( springUser.getUser().getLogin() );
        when( refreshTokenService.getUserByToken(refreshToken) ).thenReturn( springUser );

        mockMvc.perform( get( API + "/v1/users/1" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION, "Bearer " + refreshToken)
                        .param( "refreshToken", refreshToken) )
                .andDo(print())
                .andExpect( status().isNotFound() );

        assertEquals( SecurityContextHolder.getContext().getAuthentication(), authentication );
        verify( jwtService, times(1) ).verify( refreshToken );

    }
}
