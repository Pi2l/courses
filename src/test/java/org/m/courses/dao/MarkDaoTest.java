package org.m.courses.dao;

import org.junit.jupiter.api.Test;
import org.m.courses.auth.AuthManager;
import org.m.courses.builder.*;
import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.Mark;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class MarkDaoTest extends AbstractDaoTest<Mark>  {

    @Autowired
    private MarkDao markDao;

    @Autowired
    private MarkBuilder markBuilder;

    @Autowired
    private UserBuilder userBuilder;

    @Autowired
    private CourseBuilder courseBuilder;

    protected AbstractDao<Mark> getDao() {
        return markDao;
    }

    @Override
    protected Mark entityToDB() {
        return markBuilder
                .setCourse( courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB() )
                .setUser( userBuilder.setRole(Role.USER).toDB() )
                .toDB();
    }

    @Override
    protected Mark buildEntity() {
        return markBuilder
                .setCourse( courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB() )
                .setUser( userBuilder.setRole(Role.USER).toDB() )
                .build();
    }

    @Override
    protected Mark buildNewEntity() {
        return markBuilder
                .setCourse( courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB() )
                .setUser( userBuilder.setRole(Role.USER).toDB() )
                .buildNew();
    }

    @Override
    protected void assertEntitiesEqual(Mark entity, Mark entityFromDB) {
        assertEquals(entity.getCourse(), entityFromDB.getCourse());
        assertEquals(entity.getUser(), entityFromDB.getUser());
        assertEquals(entity.getValue(), entityFromDB.getValue());
    }

    @Test
    void getAsUser() {
        User user = userBuilder.setRole(Role.USER).toDB();
        User otherUser = userBuilder.setRole(Role.USER).toDB();
        Mark otherMark = markDao.create( buildNewEntity(otherUser) );
        Mark userMark = markDao.create( buildNewEntity(user) );

        AuthManager.loginAs( user );
        Mark markFromDb = markDao.get( userMark.getId() );
        assertEquals( markFromDb, userMark );

        Mark otherMarkFromDb = markDao.get( otherMark.getId() );
        assertNull( otherMarkFromDb );
    }

    protected Mark buildNewEntity(User user) {
        return markBuilder
                .setCourse( courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB() )
                .setUser( user )
                .buildNew();
    }

    @Test
    void getAsTeacher() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User otherTeacher = userBuilder.setRole(Role.TEACHER).toDB();

        Mark otherMark = markBuilder
                .setCourse( courseBuilder.setTeacher(otherTeacher).toDB() )
                .setUser( userBuilder.setRole(Role.USER).toDB() )
                .toDB();
        Mark userMark = markBuilder
                .setCourse( courseBuilder.setTeacher(teacher).toDB() )
                .setUser( userBuilder.setRole(Role.USER).toDB() )
                .toDB();

        AuthManager.loginAs( teacher );
        Mark markFromDb = markDao.get( userMark.getId() );
        assertEquals( markFromDb, userMark );

        Mark otherMarkFromDb = markDao.get( otherMark.getId() );
        assertNull( otherMarkFromDb );
    }

    @Test
    void getAsAdmin() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User otherTeacher = userBuilder.setRole(Role.TEACHER).toDB();

        Mark otherMark = markBuilder
                .setCourse( courseBuilder.setTeacher(otherTeacher).toDB() )
                .setUser( userBuilder.setRole(Role.USER).toDB() )
                .toDB();
        Mark userMark = markBuilder
                .setCourse( courseBuilder.setTeacher(teacher).toDB() )
                .setUser( userBuilder.setRole(Role.USER).toDB() )
                .toDB();

        AuthManager.loginAs( admin );
        Mark markFromDb = markDao.get( userMark.getId() );
        assertEquals( markFromDb, userMark );

        Mark otherMarkFromDb = markDao.get( otherMark.getId() );
        assertEquals( otherMarkFromDb, otherMark );
    }

    @Test
    void getAllTest() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User otherTeacher = userBuilder.setRole(Role.TEACHER).toDB();

        User user = userBuilder.setRole(Role.USER).toDB();
        User otherUser = userBuilder.setRole(Role.USER).toDB();
        Mark otherMark = markBuilder
                .setCourse( courseBuilder.setTeacher(otherTeacher).toDB() )
                .setUser( user )
                .toDB();
        Mark mark = markBuilder
                .setCourse( courseBuilder.setTeacher(teacher).toDB() )
                .setUser( otherUser )
                .toDB();

        AuthManager.loginAs( admin );
        Page<Mark> marksFromDb = markDao.getAll(Pageable.unpaged(), Specification.where(null) );
        assertEquals(2, marksFromDb.getTotalElements());

        AuthManager.loginAs( teacher );
        marksFromDb = markDao.getAll(Pageable.unpaged(), Specification.where(null) );
        assertEquals(1, marksFromDb.getTotalElements());
        assertEquals( marksFromDb.getContent().stream().findAny().get(), mark );

        AuthManager.loginAs( user );
        marksFromDb = markDao.getAll(Pageable.unpaged(), Specification.where(null) );
        assertEquals(1, marksFromDb.getTotalElements());
        assertEquals( marksFromDb.getContent().stream().findAny().get(), otherMark );
    }

    @Test
    void saveWithNullFieldsTest() {
        Mark mark = markBuilder.buildNew();
        mark.setCourse(null);
        assertNotNullField(mark);

        mark = markBuilder.buildNew();
        mark.setUser(null);
        assertNotNullField(mark);
    }

    private void assertNotNullField(Mark mark) {
        DataIntegrityViolationException exception =
                assertThrowsExactly( DataIntegrityViolationException.class, () -> markDao.create(mark) );

        String detailedCause = exception.getMessage();

        assertEquals(detailedCause, "could not execute statement; SQL [n/a]; constraint [null]; nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement" );
    }

    @Test
    void createAsTeacher() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User otherTeacher = userBuilder.setRole(Role.TEACHER).toDB();

        Mark mark = markBuilder
                .setCourse( courseBuilder.setTeacher(teacher).toDB() )
                .setUser( userBuilder.setRole(Role.USER).toDB() )
                .buildNew();

        AuthManager.loginAs( teacher );
        Mark createdMark = markDao.create( mark );
        assertEquals( createdMark, mark );

        AuthManager.loginAs( otherTeacher );
        mark.setId( null );
        assertThrowsExactly(AccessDeniedException.class, () -> markDao.create( mark ) );
    }

    @Test
    void updateAsTeacher() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User otherTeacher = userBuilder.setRole(Role.TEACHER).toDB();

        Mark mark = markBuilder
                .setCourse( courseBuilder.setTeacher(teacher).toDB() )
                .setUser( userBuilder.setRole(Role.USER).toDB() )
                .toDB();

        AuthManager.loginAs( teacher );
        Mark createdMark = markDao.update( mark );
        assertEquals( createdMark, mark );

        AuthManager.loginAs( otherTeacher );
        assertThrowsExactly(AccessDeniedException.class, () -> markDao.update( mark ) );
    }
}

