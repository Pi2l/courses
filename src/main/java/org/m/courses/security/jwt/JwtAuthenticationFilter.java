package org.m.courses.security.jwt;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private List<AntPathRequestMatcher> excludedPathMatchers;
    private RequestMatcher authRequiredPathMatcher;
    private JwtService jwtService;

    public JwtAuthenticationFilter(String authRequiredPath, String[] excludedPaths, JwtService jwtService) {
        this.authRequiredPathMatcher = new AntPathRequestMatcher( authRequiredPath );

        this.excludedPathMatchers = Stream.of(excludedPaths)
                .map(AntPathRequestMatcher::new).collect(Collectors.toList());
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if ( !requiresAuthentication(request) ) {
            filterChain.doFilter( request, response );
            return;
        }

        String header = request.getHeader( AUTHORIZATION );
        if (header == null || !header.startsWith("Bearer ") ) {
            SecurityContextHolder.getContext().setAuthentication( null );
            filterChain.doFilter( request, response );
            return;
        }

        String jwtToken = getJwt( header );
        try {
            jwtService.verify( jwtToken );

            Authentication auth = buildAuthenticationFromJwt(jwtToken);
            SecurityContextHolder.getContext().setAuthentication( auth );

            filterChain.doFilter( request, response );
        } catch (SignatureVerificationException | TokenExpiredException exception) {
            SecurityContextHolder.getContext().setAuthentication( null );
            throw exception;
        }
    }

    private Authentication buildAuthenticationFromJwt(String jwtToken) {
        UserDetails userDetails = jwtService.getUserDetailsByJwt(jwtToken);
        return new UsernamePasswordAuthenticationToken( userDetails,
                userDetails.getUsername(),
                userDetails.getAuthorities() );
    }

    private String getJwt(String header) {
        return header.substring("Bearer ".length());
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        if ( excludedPathMatchers.stream().anyMatch(el -> el.matches( request )) ) {
            return false;
        }
        return authRequiredPathMatcher.matches( request );
    }
}
