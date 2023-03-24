package org.m.courses.service;

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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserBuilder userBuilder;

    @BeforeEach
    void login() {
        AuthManager.loginAs( userBuilder.setRole(Role.ADMIN).build() );
    }

    @AfterEach
    void clearDB() {
        AuthManager.loginAs( userBuilder.setRole(Role.ADMIN).build() );
        userService.getAll().forEach(user -> userService.delete( user.getId() ) );
    }

    @Test
    void getUserTest() {
        User user = userBuilder.buildNew();
        userService.create(user);

        assertNotNull( userService.get( user.getId() ) );
    }

    @Test
    void getAllUsersTest() {
        userService.create( userBuilder.buildNew() );
        userService.create( userBuilder.buildNew() );

        assertEquals( 2, userService.getAll().size() );
    }

    @Test
    void createUserTest() {
        User user = userBuilder.buildNew();

        User createdUser = userService.create( user );

        assertNotNull( userService.get( user.getId() ) );
        assertEquals( createdUser, user );
    }

    @Test
    void updateUserTest() {
        User user = userService.create( userBuilder.buildNew() );
        User updatedUser = userBuilder.setRole(Role.ADMIN).buildNew();
        updatedUser.setId( user.getId() );

        User userFromDB = userService.update( updatedUser );

        assertEqualsUserFields(updatedUser, userFromDB);
    }

    private void assertEqualsUserFields(User updatedUser, User userFromDB) {
        assertEquals(updatedUser.getFirstName(), userFromDB.getFirstName());
        assertEquals(updatedUser.getLastName(), userFromDB.getLastName());
        assertEquals(updatedUser.getPhoneNumber(), userFromDB.getPhoneNumber());
        assertEquals(updatedUser.getLogin(), userFromDB.getLogin());
        assertEquals(updatedUser.getPassword(), userFromDB.getPassword());
        assertEquals(updatedUser.getRole(), userFromDB.getRole());
    }

    @Test
    void deleteUserTest() {
        User user = userService.create( userBuilder.buildNew() );

        userService.delete( user.getId() );

        assertThrowsExactly(AccessDeniedException.class, () -> userService.get( user.getId() ));
    }

    @Test
    void getAsNotAdminTest() {
        User admin = userService.create( userBuilder.setRole(Role.ADMIN).buildNew() );
        User teacher = userService.create( userBuilder.setRole(Role.TEACHER).buildNew() );
        User user = userService.create( userBuilder.buildNew() );

        AuthManager.loginAs( user );

        assertNotNull( userService.get( user.getId() ) );
        assertNotNull( userService.get( teacher.getId() ) );
        assertThrowsExactly(AccessDeniedException.class, () -> userService.get( admin.getId() ) );
    }

    @Test
    void getAllAsNotAdminTest() {
        userService.create( userBuilder.setRole(Role.ADMIN).buildNew() );
        userService.create( userBuilder.setRole(Role.TEACHER).buildNew() );
        User user = userService.create( userBuilder.buildNew() );

        AuthManager.loginAs( user );
        assertEquals( 2, userService.getAll().size());
    }

    @Test
    void createAsNotAdminTest() {
        User user = userService.create( userBuilder.buildNew() );

        AuthManager.loginAs( user );
        assertThrowsExactly(AccessDeniedException.class, () -> userService.create( userBuilder.setRole(Role.ADMIN).buildNew() ) );
        assertThrowsExactly(AccessDeniedException.class, () -> userService.create( userBuilder.setRole(Role.TEACHER).buildNew() ) );
        assertThrowsExactly(AccessDeniedException.class, () -> userService.create( userBuilder.buildNew() ) );
    }

    @Test
    void updateAsNotAdminTest() {
        User admin = userService.create( userBuilder.setRole(Role.ADMIN).buildNew() );
        User user = userService.create( userBuilder.buildNew() );

        User updatedUser = userBuilder.buildNew();
        User updatedAdmin = userBuilder.buildNew();
        updatedUser.setId( user.getId() );
        updatedAdmin.setId( admin.getId() );

        AuthManager.loginAs( user );

        assertThrowsExactly(AccessDeniedException.class, () -> userService.update( updatedAdmin ) );
        User updatedUserFromDB = userService.update( updatedUser );
        assertEqualsUserFields( updatedUser, updatedUserFromDB );
    }

    @Test
    void deleteAsNotAdminTest() {
        User admin = userService.create( userBuilder.setRole(Role.ADMIN).buildNew() );
        User user = userService.create( userBuilder.buildNew() );

        AuthManager.loginAs( user );

        assertThrowsExactly(AccessDeniedException.class, () -> userService.delete( admin.getId() ) );
        userService.delete( user.getId() );

        AuthManager.loginAs( admin );
        assertThrowsExactly(AccessDeniedException.class, () -> userService.get(user.getId()) );
        assertEquals( userService.get(admin.getId()), admin );
    }

}
