package org.m.courses.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.m.courses.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;


@SpringBootTest
public class UserDaoTest {

    private List<User> users;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRepository repository;

    @BeforeEach
    public void setUp() {
        initUsers();
        repository.saveAll(users);
    }

    private void initUsers() {
        User u1 = new User();
        User u2 = new User();
        users = List.of( u1, u2 );

        u1.setFirstName("Anton");
        u1.setLastName("Resheto");
        u1.setPhoneNumber("1");
        u1.setLogin("reshetoanton");
        u1.setPassword("password1");
        u1.setRole(Role.USER);

        u2.setFirstName("Petro");
        u2.setLastName("Kamin");
        u2.setPhoneNumber("2");
        u2.setLogin("kaminpetro");
        u2.setPassword("password2");
        u2.setRole(Role.USER);
    }

    @AfterEach
    public void tearDown() {
        repository.deleteAll();
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
    void getUserTest() {
        Optional<User> userOrNull = userDao.get(users.get(0).getId());

        Assertions.assertTrue(userOrNull.isPresent());
        User user = userOrNull.get();
        Assertions.assertEquals(user.getId(), users.get(0).getId());
    }

    @Test
    void getAllUsersTest() {
        List<User> users = userDao.getAll();

        Assertions.assertEquals(users.size(), this.users.size());
    }

    @Test
    void updateUserTest() {
        String firstName = "Taras";
        String lastName = "Lampa";

        User user = users.get(0);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        User userUpdated = userDao.update(user);

        Assertions.assertEquals(userUpdated.getFirstName(), firstName);
        Assertions.assertEquals(userUpdated.getLastName(), lastName);
    }

    @Test
    void deleteUserTest() {
        User userToDelete = users.get(0);

        userDao.delete(userToDelete.getId());

        Optional<User> nullUser = userDao.get(userToDelete.getId());

        Assertions.assertTrue(nullUser.isEmpty());
    }
}

