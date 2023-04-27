package org.m.courses.service;

import org.junit.jupiter.api.Test;
import org.m.courses.builder.CourseBuilder;
import org.m.courses.builder.GroupBuilder;
import org.m.courses.builder.ScheduleBuilder;
import org.m.courses.model.Role;
import org.m.courses.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;
import java.time.zone.ZoneRulesException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ScheduleServiceTest extends AbstractServiceTest<Schedule> {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ScheduleBuilder scheduleBuilder;

    @Autowired
    private CourseBuilder courseBuilder;

    @Autowired
    private GroupBuilder groupBuilder;

    @Override
    protected AbstractService<Schedule> getService() {
        return scheduleService;
    }

    @Override
    protected Schedule entityToDB() {
        return scheduleBuilder
                .setCourse( courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB() )
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
    protected void assertEntitiesEqual(Schedule e1, Schedule e2) {
    }

    @Test
    void createScheduleWithValidTimeZone() {
        Schedule schedule = buildNewEntity();
        schedule.setTimeZone("Europe/Kyiv");

        Schedule createdEntity = scheduleService.create( schedule );

        assertNotNull( getService().get( schedule.getId() ) );
        assertEquals( createdEntity, schedule );
        assertEquals( createdEntity.getTimeZone(), schedule.getTimeZone() );
    }

    @Test
    void createScheduleWithInvalidTimeZone() {
        Schedule schedule = buildNewEntity();
        schedule.setTimeZone("dwe");

        Exception exception =
                assertThrowsExactly(ZoneRulesException.class, () -> scheduleService.create( schedule ) );

        assertNull( getService().get( schedule.getId() ) );
        assertEquals(exception.getMessage(), "Unknown time-zone ID: dwe" );
    }

    @Test
    void createScheduleThatStartsAfterItEnds() {
        Schedule schedule = buildNewEntity();
        schedule.setStartAt( schedule.getEndAt().plusDays( 1 ));

        Exception exception =
                assertThrowsExactly(IllegalArgumentException.class, () -> scheduleService.create( schedule ) );

        assertNull( getService().get( schedule.getId() ) );
        assertEquals(exception.getMessage(), "startAt must be before endAt" );
    }

    @Test
    void createScheduleThatAlreadyEnded() {
        Schedule schedule = buildNewEntity();
        schedule.setStartAt( ZonedDateTime.now().minusDays( 2 ));
        schedule.setEndAt( ZonedDateTime.now().minusDays( 1 ));

        Exception exception =
                assertThrowsExactly(IllegalArgumentException.class, () -> scheduleService.create( schedule ) );

        assertNull( getService().get( schedule.getId() ) );
        assertEquals(exception.getMessage(), "endAt must be after now" );
    }

    @Test
    void updateScheduleTimeZone() {
        Schedule oldSchedule = entityToDB();
        Schedule schedule = buildNewEntity();
        schedule.setId( oldSchedule.getId() );
        schedule.setTimeZone( "Europe/Berlin" );

        Exception exception =
                assertThrowsExactly(IllegalArgumentException.class, () -> scheduleService.update( schedule ) );

        assertEquals(exception.getMessage(), "can not modify time zone" );
    }

    @Test
    void updateScheduleThatStartsAfterItEnds() {
        Schedule oldSchedule = entityToDB();
        Schedule schedule = buildNewEntity();
        schedule.setId( oldSchedule.getId() );

        schedule.setStartAt( schedule.getEndAt().plusDays( 1 ) );

        Exception exception =
                assertThrowsExactly(IllegalArgumentException.class, () -> scheduleService.update( schedule ) );

        assertEquals(exception.getMessage(), "startAt must be before endAt" );
    }

    @Test
    void updateScheduleThatAlreadyEnded() {
        Schedule oldSchedule = entityToDB();
        Schedule schedule = buildNewEntity();
        schedule.setId( oldSchedule.getId() );

        schedule.setStartAt( ZonedDateTime.now().minusDays( 2 ));
        schedule.setEndAt( ZonedDateTime.now().minusDays( 1 ));

        Exception exception =
                assertThrowsExactly(IllegalArgumentException.class, () -> scheduleService.update( schedule ) );

        assertEquals(exception.getMessage(), "endAt must be after now" );
    }

}
