package org.m.courses.service;

import org.m.courses.dao.AbstractDao;
import org.m.courses.dao.UserDao;
import org.m.courses.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService extends AbstractService<User> {

    private UserDao userDao;
    private UserAuthorizationService userAuthorizationService;

    private BCryptPasswordEncoder passwordEncoder;

    public UserService(UserDao userDao, UserAuthorizationService userAuthorizationService, BCryptPasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.userAuthorizationService = userAuthorizationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected AbstractDao<User> getDao() {
        return userDao;
    }

    @Override
    public User create(User user) {
        user.setPassword( passwordEncoder.encode( user.getPassword() ) );
        return super.create( user );
    }

    @Override
    public User update(User user) {
        User oldUser = userDao.get( user.getId() );
        if (oldUser != null) {
            verifyRole( oldUser, user );

            String password = user.getPassword();
            if (oldUser.getPassword().equals(password)) {
                return super.update( user );
            }

            if (password == null || password.isEmpty()) {
                user.setPassword( oldUser.getPassword() );
            } else {
                user.setPassword( passwordEncoder.encode( user.getPassword() ) );
            }
        } else {
            return super.update( null );
        }

        return super.update( user );
    }

    private void verifyRole(User oldUser, User user) {
        if ( !( userAuthorizationService.isAdmin() || oldUser.getRole().equals( user.getRole() )) ) {
            throw new IllegalArgumentException("roles have to be the same");
        }
    }

    public boolean isUnique(User user) {
        Optional<User> other = userDao.findByLogin(user.getLogin());

        return other.isEmpty() || other.get().getId().equals(user.getId());
    }
}
