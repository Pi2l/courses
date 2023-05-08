package org.m.courses.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.m.courses.builder.CourseBuilder;
import org.m.courses.builder.GroupBuilder;
import org.m.courses.builder.ScheduleBuilder;
import org.m.courses.model.Course;
import org.m.courses.model.Group;
import org.m.courses.model.Role;
import org.m.courses.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ScheduleServiceTest extends AbstractServiceTest<Schedule> {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private GroupService groupService;

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
        Course course = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        return scheduleBuilder
                .setCourse( course )
                .setGroup( groupBuilder.setCourses( Set.of(course) ).toDB() )
                .toDB();
    }

    @Override
    protected Schedule buildEntity() {
        Course course = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        return scheduleBuilder
                .setCourse( course )
                .setGroup( groupBuilder.setCourses( Set.of(course) ).toDB() )
                .build();
    }

    @Override
    protected Schedule buildNewEntity() {
        Course course = courseBuilder.setTeacher(userBuilder.setRole(Role.TEACHER).toDB()).toDB();
        return scheduleBuilder
                .setCourse( course )
                .setGroup( groupBuilder.setCourses( Set.of(course) ).toDB() )
                .buildNew();
    }

    @AfterEach
    void clearDB() {
        super.clearDB();
        groupService.getAll().forEach(entity -> groupService.delete( entity.getId() ) );
    }


    @Override
    protected void assertEntitiesEqual(Schedule e1, Schedule e2) {
    }

    @Test
    void createScheduleWithValidTimeZone() {
        Schedule schedule = buildNewEntity();

        Schedule createdEntity = scheduleService.create( schedule );

        assertNotNull( getService().get( schedule.getId() ) );
        assertEquals( createdEntity, schedule );
    }

    @Test
    void createScheduleWithinDiffTimeZones() {
        Schedule schedule = buildNewEntity();
        LocalDateTime localNow = LocalDateTime.now();
        schedule.setStartAt( ZonedDateTime.of( localNow, ZoneId.of("Europe/Kyiv") ).plusHours(2) );
        schedule.setEndAt( ZonedDateTime.of( localNow, ZoneId.of("Europe/Berlin") ).plusHours(2) );

        Schedule createdEntity = scheduleService.create( schedule );

        assertNotNull( getService().get( schedule.getId() ) );
        assertEquals( createdEntity, schedule );
    }

    @Test
    void createScheduleForCourseThat() {
        Schedule schedule = buildNewEntity();
        LocalDateTime localNow = LocalDateTime.now();
        schedule.setStartAt( ZonedDateTime.of( localNow, ZoneId.of("Europe/Kyiv") ).plusHours(2) );
        schedule.setEndAt( ZonedDateTime.of( localNow, ZoneId.of("Europe/Berlin") ).plusHours(2) );

        Schedule createdEntity = scheduleService.create( schedule );

        assertNotNull( getService().get( schedule.getId() ) );
        assertEquals( createdEntity, schedule );
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
    void createScheduleWithCourseThatBelongsToGroup() {
        Course course = courseBuilder.toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();

        Schedule schedule = scheduleBuilder
                .setGroup( group )
                .setCourse( course ).buildNew();

        Schedule createdEntity = scheduleService.create( schedule );

        assertNotNull( getService().get( schedule.getId() ) );
        assertEquals( createdEntity, schedule );
    }

    @Test
    void createScheduleWithCourseThatDoesNotBelongToGroup() {
        Course otherCourse = courseBuilder.toDB();
        Course course = courseBuilder.toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();

        Schedule schedule = scheduleBuilder
                .setGroup( group )
                .setCourse( otherCourse ).buildNew();

        IllegalArgumentException exception =
                assertThrowsExactly(IllegalArgumentException.class, () -> scheduleService.create( schedule ) );

        assertEquals("group has not such course with courseId = " + otherCourse.getId(), exception.getMessage());
        assertNull( getService().get( schedule.getId() ) );
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

    @Test
    void updateScheduleWithinDiffTimeZones() {
        Schedule oldSchedule = entityToDB();
        Schedule schedule = buildNewEntity();
        schedule.setId( oldSchedule.getId() );

        LocalDateTime localNow = LocalDateTime.now();
        schedule.setStartAt( ZonedDateTime.of( localNow, ZoneId.of("Europe/Kyiv") ).plusHours(2) );
        schedule.setEndAt( ZonedDateTime.of( localNow, ZoneId.of("Europe/Berlin") ).plusHours(2) );

        Schedule createdEntity = scheduleService.update( schedule );

        assertNotNull( getService().get( schedule.getId() ) );
        assertEquals( createdEntity, schedule );
    }

    @Test
    void updateScheduleWithCourseThatBelongsToGroup() {
        Course course = courseBuilder.toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();

        Schedule schedule = scheduleBuilder
                .setGroup( group )
                .setCourse( course ).toDB();

        Schedule createdEntity = scheduleService.update( schedule );

        assertNotNull( getService().get( schedule.getId() ) );
        assertEquals( createdEntity, schedule );
    }

    @Test
    void updateScheduleWithCourseThatDoesNotBelongToGroup() {
        Course otherCourse = courseBuilder.toDB();
        Course course = courseBuilder.toDB();
        Group group = groupBuilder.setCourses( Set.of(course) ).toDB();

        Schedule schedule = scheduleBuilder
                .setGroup( group )
                .setCourse( otherCourse ).toDB();

        IllegalArgumentException exception =
                assertThrowsExactly(IllegalArgumentException.class, () -> scheduleService.update( schedule ) );

        assertEquals("group has not such course with courseId = " + otherCourse.getId(), exception.getMessage());
    }

}
