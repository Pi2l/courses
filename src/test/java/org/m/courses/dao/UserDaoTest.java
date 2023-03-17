package org.m.courses.dao;

import org.junit.jupiter.api.Test;
import org.m.courses.builder.UserBuilder;
import org.m.courses.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import javax.persistence.Column;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserBuilder userBuilder;

    @Test
    void saveUserTest() {
        User user = userBuilder.buildNew();

        userDao.create(user);

        assertNotNull(userDao.get(user.getId()));
    }

    @Test
    void saveUserWithNullFieldsTest() {
        User user = new User();

        DataIntegrityViolationException exception =
                assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.create(user) );

        String detailedCause = exception.getMessage();

        var fieldsToCheck = Arrays.stream(User.class.getDeclaredFields())
                .filter(field -> {
                    Column columnAnnotation = field.getAnnotation(Column.class);
                    if (columnAnnotation != null) {
                        return !columnAnnotation.nullable();
                    } else {
                        return false;
                    }
                }).collect(Collectors.toList());

        AtomicBoolean nullableFieldViolated = new AtomicBoolean(false);
        fieldsToCheck.forEach(field ->
                nullableFieldViolated.set(nullableFieldViolated.get() | detailedCause.contains(field.getName())));
        assertTrue(nullableFieldViolated.get());
    }

    @Test
    void saveUserWithPhoneNumberGreaterThanAllowedTest() {
        User user = userBuilder.buildNew();
        user.setPhoneNumber("123456789012345678901234");

        DataIntegrityViolationException exception =
                assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.create(user) );

        String detailedMessage = exception.getCause().getCause().getMessage();
        assertTrue(detailedMessage.contains("Value too long for column"));
        assertTrue(detailedMessage.contains( user.getPhoneNumber() ));
    }

    @Test
    void saveUserWithNonUniqueLoginTest() {
        User userFromDB = userBuilder.toDB();
        User userWithSameLogin = userBuilder.buildNew();
        userWithSameLogin.setLogin( userFromDB.getLogin() );

        DataIntegrityViolationException exception =
                assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.create(userWithSameLogin) );

        String detailedMessage = exception.getCause().getCause().getMessage();
        assertTrue(detailedMessage.contains("Unique index or primary key violation"));
        assertTrue(detailedMessage.contains( userWithSameLogin.getLogin() ));
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

        Optional<User> userFromDBOrNull = userDao.update(updatedUser);
        assertFalse(userFromDBOrNull.isEmpty());

        User userFromDB = userFromDBOrNull.get();

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

        Optional<User> updatedUser = userDao.update(user);
        assertTrue(updatedUser.isEmpty());
    }

    @Test
    void updateUserWithNullFieldsTest() {
        User user = userBuilder.toDB();
        User userWithNullFields = new User();
        userWithNullFields.setId( user.getId() );

        assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.update(userWithNullFields) );
    }

    @Test
    void updateUserWithPhoneNumberGreaterThanAllowedTest() {
        User user = userBuilder.toDB();
        user.setPhoneNumber("123456789012345678901234");

        assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.update(user) );
    }

    @Test
    void updateUserWithNonUniqueLoginTest() {
        User userFromDB = userBuilder.toDB();
        User userWithSameLogin = userBuilder.toDB();
        userWithSameLogin.setLogin( userFromDB.getLogin() );

        assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.update(userWithSameLogin) );
    }

    @Test
    void deleteUserTest() {
        User userToDelete = userBuilder.toDB();

        userDao.delete(userToDelete.getId());

        Optional<User> userFromDB = userDao.get(userToDelete.getId());

        assertTrue(userFromDB.isEmpty());
    }
}

