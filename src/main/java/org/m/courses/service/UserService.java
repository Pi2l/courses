package org.m.courses.service;

import org.m.courses.dao.AbstractDao;
import org.m.courses.dao.UserDao;
import org.m.courses.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService extends AbstractService<User> {

    private UserDao userDao;

    private BCryptPasswordEncoder passwordEncoder;

    public UserService(UserDao userDao, BCryptPasswordEncoder passwordEncoder) {
        this.userDao = userDao;
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
        user.setPassword( passwordEncoder.encode( user.getPassword() ) );
        return super.update( user );
    }

}
