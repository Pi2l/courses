package org.m.courses.service;

import org.m.courses.dao.UserDao;
import org.m.courses.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements IService<User> {

    private UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public List<User> getAll() {
        return userDao.getAll();
    }

    @Override
    public User get(Long id) {
        return userDao.get(id).orElse(null);
    }

    @Override
    public User create(User instance) {
        return userDao.create( instance ).orElse(null);
    }

    @Override
    public User update(User instance) {
        return userDao.update( instance ).orElse(null);
    }

    @Override
    public void delete(Long id) {
        userDao.delete(id);
    }
}
