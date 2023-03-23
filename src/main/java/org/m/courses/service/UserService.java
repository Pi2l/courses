package org.m.courses.service;

import org.m.courses.dao.UserDao;
import org.m.courses.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<User> getAll() {
        return userDao.getAll();
    }

    public User get(Long id) {
        return userDao.get(id).orElse(null);
    }

    public User create(User instance) {
        return userDao.create( instance );
    }

    public User update(User instance) {
        return userDao.update( instance );
    }

    public void delete(Long id) {
        userDao.delete(id);
    }
}
