package org.m.courses.service;

import org.m.courses.dao.UserDao;
import org.m.courses.model.SpringUser;
import org.m.courses.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    private UserDao userDao;

    public UserDetailsServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userDao.findByLogin(username);

        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException(String.format("user with login: %s now found", (username)));
        }

        return new SpringUser( userOptional.get() );
    }
}
