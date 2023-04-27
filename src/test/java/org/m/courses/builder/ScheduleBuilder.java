package org.m.courses.builder;

import org.m.courses.dao.ScheduleDao;
import org.m.courses.model.Course;
import org.m.courses.model.Group;
import org.m.courses.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;


@Component
public class ScheduleBuilder {

    @Autowired private ScheduleDao scheduleDao;

    private Schedule schedule;

    private Group group;
    private Course course;

    public static ScheduleBuilder builder() {
        return new ScheduleBuilder();
    }

    public ScheduleBuilder() {
        initDefaultUser();
    }

    public Schedule build() {
        Schedule odlEntity = schedule;
        initDefaultUser();
        return odlEntity;
    }

    public Schedule buildNew() {
        return setId(null)
                .build();
    }

    // Spring based
    public Schedule toDB() {
        return scheduleDao.create( buildNew() );
    }

    private ScheduleBuilder initDefaultUser() {
        long randomValue = Math.abs(new SecureRandom().nextLong()) % 1000000;
        this.schedule = new Schedule();

        setId(randomValue);
        setCourse( course );
        setGroup( group );

        ZonedDateTime startAt = ZonedDateTime.now().plusMinutes( randomValue );
        setStartAt( startAt );
        setEndAt( startAt.plusMinutes(randomValue) );
        setTimeZone( ZoneId.of("Europe/Kyiv") );
        return this;
    }

    public ScheduleBuilder setId(Long id) {
        schedule.setId(id);
        return this;
    }

    public ScheduleBuilder setCourse(Course course) {
        schedule.setCourse(course);
        return this;
    }

    public ScheduleBuilder setGroup(Group group) {
        schedule.setGroup(group);
        return this;
    }

    public ScheduleBuilder setStartAt(ZonedDateTime startAt) {
        schedule.setStartAt(startAt);
        return this;
    }

    public ScheduleBuilder setEndAt(ZonedDateTime endAt) {
        schedule.setEndAt(endAt);
        return this;
    }

    public ScheduleBuilder setTimeZone(ZoneId zoneId) {
        schedule.setTimeZone( zoneId.toString() );
        return this;
    }
}
