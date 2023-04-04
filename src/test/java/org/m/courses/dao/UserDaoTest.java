package org.m.courses.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.m.courses.auth.AuthManager;
import org.m.courses.builder.UserBuilder;
import org.m.courses.exception.AccessDeniedException;
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
    void login() {
        AuthManager.loginAs( userBuilder.setRole(Role.ADMIN).build() );
    }

    @Test
    void saveUserTest() {
        User user = userBuilder.buildNew();

        userDao.create(user);

        assertNotNull( userDao.get(user.getId()) );
    }

    @Test
    void saveUserWithNullFieldsTest() {
        User user = userBuilder.buildNew();
        user.setFirstName(null);
        assertNotNullField(user, "firstName");
        
        user = userBuilder.buildNew();
        user.setLastName(null);
        assertNotNullField(user, "lastName");
  
        user = userBuilder.buildNew();
        user.setPhoneNumber(null);
        assertNotNullField(user, "phoneNumber");
   
        user = userBuilder.buildNew();
        user.setLogin(null);
        assertNotNullField(user, "login");
   
        user = userBuilder.buildNew();
        user.setPassword(null);
        assertNotNullField(user, "password");
   
        user = userBuilder.buildNew();
        user.setRole(null);
        assertNotNullField(user, "role");
    }

    private void assertNotNullField(User user, String fieldName) {
        DataIntegrityViolationException exception =
                assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.create(user) );

        String detailedCause = exception.getMessage();

        assertTrue(detailedCause.contains( fieldName ));
        assertTrue(detailedCause.contains( "not-null property references a null or transient value" ));
    }

    @Test
    void saveUserWithPhoneNumberGreaterThanAllowedTest() {
        User user = userBuilder.buildNew();
        user.setPhoneNumber("123456789012345678901234");

        DataIntegrityViolationException exception =
                assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.create(user) );

        String detailedMessage = exception.getCause().getCause().getMessage();
        assertTrue(detailedMessage.contains( "Value too long for column" ));
        assertTrue(detailedMessage.contains( "phone_number" ));
    }

    @Test
    void saveUserWithNonUniqueLoginTest() {
        User userFromDB = userBuilder.toDB();
        User userWithSameLogin = userBuilder.buildNew();
        userWithSameLogin.setLogin( userFromDB.getLogin() );

        DataIntegrityViolationException exception =
                assertThrowsExactly( DataIntegrityViolationException.class, () -> userDao.create(userWithSameLogin) );

        String detailedMessage = exception.getCause().getCause().getMessage();
        assertTrue(detailedMessage.contains( "Unique index or primary key violation" ));
        assertTrue(detailedMessage.contains( "login" ));
    }

    @Test
    void getUserTest() {
        User user = userBuilder.toDB();
        User userFromDB = userDao.get(user.getId());

        assertEquals(user, userFromDB);
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
        assertNotNull(userFromDB);

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

        assertThrowsExactly(AccessDeniedException.class, () -> userDao.update(user) );
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

        assertThrowsExactly(AccessDeniedException.class, () -> userDao.get(userToDelete.getId()) );

    }
}

