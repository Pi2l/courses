package org.m.courses.dao;

import org.junit.jupiter.api.Test;
import org.m.courses.builder.UserBuilder;
import org.m.courses.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class UserDaoTest extends AbstractDaoTest<User>  {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserBuilder userBuilder;

    protected AbstractDao<User> getDao() {
        return userDao;
    }

    @Override
    protected User entityToDB() {
        return userBuilder.toDB();
    }

    @Override
    protected User buildEntity() {
        return userBuilder.build();
    }

    @Override
    protected User buildNewEntity() {
        return userBuilder.buildNew();
    }

    @Override
    protected void assertEntitiesEqual(User updatedUser, User userFromDB) {
        assertEquals(updatedUser.getFirstName(), userFromDB.getFirstName());
        assertEquals(updatedUser.getLastName(), userFromDB.getLastName());
        assertEquals(updatedUser.getPhoneNumber(), userFromDB.getPhoneNumber());
        assertEquals(updatedUser.getLogin(), userFromDB.getLogin());
        assertEquals(updatedUser.getPassword(), userFromDB.getPassword());
        assertEquals(updatedUser.getRole(), userFromDB.getRole());
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
    void updateNotExistingUserTest() {
        User user = userBuilder.build();

        assertNull( userDao.update(user) );
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

}

