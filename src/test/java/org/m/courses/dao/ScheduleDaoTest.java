package org.m.courses.dao;

import org.junit.jupiter.api.Test;
import org.m.courses.auth.AuthManager;
import org.m.courses.builder.CourseBuilder;
import org.m.courses.builder.GroupBuilder;
import org.m.courses.builder.ScheduleBuilder;
import org.m.courses.builder.UserBuilder;
import org.m.courses.exception.AccessDeniedException;
import org.m.courses.model.Role;
import org.m.courses.model.Schedule;
import org.m.courses.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class ScheduleDaoTest extends AbstractDaoTest<Schedule>  {

    @Autowired
    private ScheduleDao scheduleDao;

    @Autowired
    private ScheduleBuilder scheduleBuilder;

    @Autowired
    private UserBuilder userBuilder;

    @Autowired
    private CourseBuilder courseBuilder;

    @Autowired
    private GroupBuilder groupBuilder;

    protected AbstractDao<Schedule> getDao() {
        return scheduleDao;
    }

    @Override
    protected Schedule entityToDB() {
        return scheduleBuilder
                .setCourse( courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB() )
                .setGroup( groupBuilder.toDB() )
                .toDB();
    }

    protected Schedule entityToDB(User teacher) {
        return scheduleBuilder
                .setCourse( courseBuilder.setTeacher(teacher).toDB() )
                .setGroup( groupBuilder.toDB() )
                .toDB();
    }

    @Override
    protected Schedule buildEntity() {
        return scheduleBuilder
                .setCourse( courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB() )
                .setGroup( groupBuilder.toDB() )
                .build();
    }

    @Override
    protected Schedule buildNewEntity() {
        return scheduleBuilder
                .setCourse( courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB() )
                .setGroup( groupBuilder.toDB() )
                .buildNew();
    }

    @Override
    protected void assertEntitiesEqual(Schedule entity, Schedule entityFromDB) {
        assertEquals(entity.getStartAt(), entityFromDB.getStartAt());
        assertEquals(entity.getEndAt(), entityFromDB.getEndAt());
        assertEquals(entity.getGroup(), entityFromDB.getGroup());
        assertEquals(entity.getCourse(), entityFromDB.getCourse());
    }

    @Test
    void saveWithNullFieldsTest() {
        Schedule schedule = scheduleBuilder.buildNew();
        schedule.setGroup(null);
        assertNotNullField(schedule, "group");

        schedule = scheduleBuilder.buildNew();
        schedule.setCourse(null);
        assertNotNullField(schedule, "course");

        schedule = scheduleBuilder.buildNew();
        schedule.setStartAt(null);
        assertNotNullField(schedule, "startAt");

        schedule = scheduleBuilder.buildNew();
        schedule.setEndAt(null);
        assertNotNullField(schedule, "endAt");

    }

    private void assertNotNullField(Schedule schedule, String fieldName) {
        DataIntegrityViolationException exception =
                assertThrowsExactly( DataIntegrityViolationException.class, () -> scheduleDao.create(schedule) );

        String detailedCause = exception.getMessage();

        assertTrue(detailedCause.equals( "could not execute statement; SQL [n/a]; constraint [null]; nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement" ));
    }

    @Test
    void createAsTeacher() {
        Schedule schedule = buildNewEntity();
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).toDB();

        AuthManager.loginAs( teacher );
        schedule.getCourse().setTeacher( teacher );
        Schedule createdSchedule = scheduleDao.create( schedule );

        assertNotNull( createdSchedule );

        AuthManager.loginAs( admin );
        Schedule scheduleWithoutTeacher = buildNewEntity();
        scheduleWithoutTeacher.getCourse().setTeacher( userBuilder.toDB() );

        AuthManager.loginAs( teacher );

        assertThrowsExactly(AccessDeniedException.class, () -> scheduleDao.create( scheduleWithoutTeacher ) );
        scheduleWithoutTeacher.getCourse().setTeacher( null );
        assertThrowsExactly(AccessDeniedException.class, () -> scheduleDao.create( scheduleWithoutTeacher ) );

        AuthManager.loginAs( admin );
    }

    @Test
    void updateAsTeacher() {
        User teacher = userBuilder.setRole(Role.TEACHER).toDB();
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        Schedule oldSchedule = entityToDB(teacher);

        Schedule schedule = buildNewEntity();
        schedule.setId( oldSchedule.getId() );

        AuthManager.loginAs( teacher );
        schedule.getCourse().setTeacher( teacher );
        Schedule updatedSchedule = scheduleDao.update( schedule );

        assertNotNull( updatedSchedule );

        AuthManager.loginAs( admin );
        Schedule scheduleWithoutTeacher = buildNewEntity();
        scheduleWithoutTeacher.setId( oldSchedule.getId() );
        scheduleWithoutTeacher.getCourse().setTeacher( userBuilder.toDB() );

        AuthManager.loginAs( teacher );

        assertThrowsExactly(AccessDeniedException.class, () -> scheduleDao.update( scheduleWithoutTeacher ) );
        scheduleWithoutTeacher.getCourse().setTeacher( null );
        assertThrowsExactly(AccessDeniedException.class, () -> scheduleDao.update( scheduleWithoutTeacher ) );

        AuthManager.loginAs( admin );
    }
}

