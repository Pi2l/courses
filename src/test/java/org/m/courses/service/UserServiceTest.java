package org.m.courses.service;

import org.junit.jupiter.api.Test;
import org.m.courses.auth.AuthManager;
import org.m.courses.builder.UserBuilder;
import org.m.courses.exception.AccessDeniedException;
import org.m.courses.filtering.FilteringOperation;
import org.m.courses.filtering.SearchCriteria;
import org.m.courses.filtering.specification.EqualSpecificationBuilder;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTest extends AbstractServiceTest<User> {

    @Autowired
    private UserService userService;

    @Autowired
    private UserBuilder userBuilder;

    @Test
    void getAsNotAdminTest() {
        User admin = userService.create( userBuilder.setRole(Role.ADMIN).buildNew() );
        User teacher = userService.create( userBuilder.setRole(Role.TEACHER).buildNew() );
        User user = userService.create( userBuilder.buildNew() );

        AuthManager.loginAs( user );

        assertNotNull( userService.get( user.getId() ) );
        assertNotNull( userService.get( teacher.getId() ) );
        assertNull( userService.get( admin.getId() ) );
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
    void getPageAsAdminTest() {
        userService.create( userBuilder.setRole(Role.ADMIN).buildNew() );
        userService.create( userBuilder.setRole(Role.TEACHER).buildNew() );
        userService.create( userBuilder.buildNew() );

        Page<User> result = userService.getAll(Pageable.unpaged(), null);
        assertEquals( 3, result.getTotalElements());
        assertEquals( 3, result.getContent().size());
    }

    @Test
    void getPageAsNotAdminTest() {
        userService.create( userBuilder.setRole(Role.ADMIN).buildNew() );
        userService.create( userBuilder.setRole(Role.TEACHER).buildNew() );
        User user = userService.create( userBuilder.buildNew() );

        AuthManager.loginAs( user );
        Page<User> result = userService.getAll(Pageable.unpaged(), null);
        assertEquals( 2, result.getTotalElements());
        assertEquals( 2, result.getContent().size());
        assertFalse( result.getContent().stream().anyMatch( item -> Role.ADMIN.equals(item.getRole())));
    }

    @Test
    void getAdminThroughFilterAsNotAdminTest() {
        userService.create( userBuilder.setRole(Role.ADMIN).buildNew() );
        userService.create( userBuilder.setRole(Role.TEACHER).buildNew() );
        User user = userService.create( userBuilder.buildNew() );

        AuthManager.loginAs( user );
        Page<User> result = userService.getAll(Pageable.unpaged(),
                new EqualSpecificationBuilder<User>().buildSpecification( new SearchCriteria("role", FilteringOperation.NOT_EQUAL, Role.TEACHER) ) );

        assertEquals( 1, result.getTotalElements());
        assertEquals( 1, result.getContent().size());
        assertFalse( result.getContent().stream().anyMatch( item -> Role.ADMIN.equals(item.getRole())));
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
    void ensurePasswordIsEncryptedAfterCreateTest() {
        User user = userBuilder.buildNew();
        String passwd = user.getPassword();
        User createdUser = userService.create( user );

        assertTrue( BCrypt.checkpw(passwd, createdUser.getPassword()) );
    }


    @Test
    void ensurePasswordIsEncryptedAfterUpdateTest() {
        User user = userBuilder.buildNew();
        User createdUser = userService.create( user );
        String passwd = "passwd";

        createdUser.setPassword(passwd);
        User updatedUser = userService.update(createdUser);

        assertTrue( BCrypt.checkpw(passwd, updatedUser.getPassword()) );
    }

    @Test
    void updatePasswordThatIsAlreadyEncryptedTest() {
        User user = userBuilder.buildNew();
        user = userService.create( user );
        String oldPasswd = user.getPassword();

        user = userBuilder
                .setId(user.getId())
                .setPassword(user.getPassword()).build();


        User updatedUser = userService.update( user );

        assertEquals( user.getPassword(), updatedUser.getPassword() );
        assertEquals( oldPasswd, updatedUser.getPassword() );
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

        assertThrowsExactly(IllegalArgumentException.class, () -> userService.update( updatedAdmin ) );
        User updatedUserFromDB = userService.update( updatedUser );
        assertEntitiesEqual( updatedUser, updatedUserFromDB );
    }

    @Test
    void updateRoleAsNotAdminTest() {
        User user = userService.create( userBuilder.setRole(Role.USER).buildNew() );
        User admin = userService.create( userBuilder.setRole(Role.ADMIN).buildNew() );

        User userWithAdminRole = userBuilder
                .setId( user.getId() )
                .setRole(Role.ADMIN)
                .build();
        User userWithTeacherRole = userBuilder
                .setId( user.getId() )
                .setRole(Role.TEACHER)
                .build();

        AuthManager.loginAs( user );
        Throwable ex = assertThrowsExactly(IllegalArgumentException.class, () -> userService.update( userWithAdminRole ) );
        assertEquals(ex.getMessage(), "roles have to be the same");
        ex = assertThrowsExactly(IllegalArgumentException.class, () -> userService.update( userWithTeacherRole ) );
        assertEquals(ex.getMessage(), "roles have to be the same");

        AuthManager.loginAs( admin );
        User updatedUserWithTeacherRole = userService.update( userWithTeacherRole );
        assertEntitiesEqual( userWithTeacherRole, updatedUserWithTeacherRole );

        User updatedUserWithAdminRole = userService.update( userWithAdminRole );
        assertEntitiesEqual( userWithAdminRole, updatedUserWithAdminRole  );
    }

    @Test
    void deleteAsNotAdminTest() {
        User admin = userService.create( userBuilder.setRole(Role.ADMIN).buildNew() );
        User user = userService.create( userBuilder.buildNew() );

        AuthManager.loginAs( user );

        assertThrowsExactly(AccessDeniedException.class, () -> userService.delete( admin.getId() ) );
        userService.delete( user.getId() );
        assertNull( userService.get( user.getId()) );

        AuthManager.loginAs( admin );
        assertEquals( userService.get(admin.getId()), admin );
    }

    @Test
    void isLoginUniqueTest() {
        User user1 = userService.create( userBuilder.buildNew() );
        User user2 = userService.create( userBuilder.buildNew() );

        user2.setLogin( user1.getLogin() );

        assertTrue( userService.isUnique(user1) );
        assertFalse( userService.isUnique(user2) );
    }

    @Override
    protected AbstractService<User> getService() {
        return userService;
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
    protected void assertEntitiesEqual(User e1, User e2) {

    }
}
