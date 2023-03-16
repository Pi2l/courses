package org.m.courses.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.m.courses.builder.UserBuilder;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserBuilder userBuilder;

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    void saveUserTest() {
        User user = new User();
        user.setFirstName("Mykhailo");
        user.setLastName("Ostapenko");
        user.setPhoneNumber("88317232");
        user.setRole(Role.USER);
        user.setLogin("login1");
        user.setPassword("password");

        userDao.create(user);

        Assertions.assertNotNull(userDao.get(user.getId()));
    }

    @Test
    void saveUserWithNullFieldsTest() {
        User user = new User();

        Assertions.assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.create(user) );
    }

    @Test
    void saveUserWithPhoneNumberGreaterThanAllowedTest() {
        User user = userBuilder.buildNew();
        user.setPhoneNumber("123456789012345678901234");

        Assertions.assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.create(user) );
    }

    @Test
    void saveUserWithNonUniqueLoginTest() {
        User userFromDB = userBuilder.toDB();
        User userWithSameLogin = userBuilder.buildNew();
        userWithSameLogin.setLogin( userFromDB.getLogin() );

        Assertions.assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.create(userWithSameLogin) );
    }

    @Test
    void getUserTest() {
        User user = userBuilder.toDB();
        Optional<User> userFromDB = userDao.get(user.getId());

        assertEquals(user, userFromDB.get());
    }

    @Test
    void getAllUsersTest() {
        userBuilder.toDB();
        userBuilder.toDB();

        List<User> users = userDao.getAll();

        assertEquals(2, users.size());
    }

    @Test
    void updateUserTest() {
        User user = userBuilder.toDB();
        User updatedUser = userBuilder.build();
        updatedUser.setId(user.getId());

        User userFromDB = userDao.update(updatedUser);

        assertEquals(updatedUser.getFirstName(), userFromDB.getFirstName());
        assertEquals(updatedUser.getLastName(), userFromDB.getLastName());
        assertEquals(updatedUser.getPhoneNumber(), userFromDB.getPhoneNumber());
        assertEquals(updatedUser.getLogin(), userFromDB.getLogin());
        assertEquals(updatedUser.getPassword(), userFromDB.getPassword());
        assertEquals(updatedUser.getRole(), userFromDB.getRole());
    }

    @Test
    void updateNotExistingUserTest() {
        User user = userBuilder.build();

        User updatedUser = userDao.update(user);
        assertEquals(updatedUser, user);
        assertTrue( userDao.get( user.getId() ).isEmpty() );
    }

    @Test
    void updateUserWithNullFieldsTest() {
        User user = userBuilder.toDB();
        userDao.create(user);

        user.setFirstName(null);
        user.setLastName(null);
        user.setPhoneNumber(null);
        user.setLogin(null);
        user.setPassword(null);
        user.setRole(null);
        Assertions.assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.update(user) );
    }

    @Test
    void updateUserWithPhoneNumberGreaterThanAllowedTest() {
        User user = userBuilder.toDB();
        user.setPhoneNumber("123456789012345678901234");

        Assertions.assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.update(user) );
    }

    @Test
    void updateUserWithNonUniqueLoginTest() {
        User userFromDB = userBuilder.toDB();
        User userWithSameLogin = userBuilder.toDB();
        userWithSameLogin.setLogin( userFromDB.getLogin() );

        Assertions.assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.update(userWithSameLogin) );
    }

    @Test
    void deleteUserTest() {
        User userToDelete = userBuilder.toDB();

        userDao.delete(userToDelete.getId());

        Optional<User> userFromDB = userDao.get(userToDelete.getId());

        Assertions.assertTrue(userFromDB.isEmpty());
    }
}

