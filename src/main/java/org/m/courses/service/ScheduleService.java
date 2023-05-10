package org.m.courses.service;

import org.m.courses.dao.AbstractDao;
import org.m.courses.dao.ScheduleDao;
import org.m.courses.model.Schedule;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class ScheduleService extends AbstractService<Schedule> {

    private ScheduleDao scheduleDao;

    public ScheduleService(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
    }

    @Override
    protected AbstractDao<Schedule> getDao() {
        return scheduleDao;
    }

    @Override
    public Schedule create(Schedule entity) {
        validate( entity );
        return super.create( entity );
    }

    private void validate(Schedule schedule) {
        ZoneId zoneId = schedule.getEndAt().getZone();

        if ( schedule.getEndAt().isBefore( ZonedDateTime.now(zoneId) ) ) {
            throw new IllegalArgumentException("endAt must be after now");
        }
        if ( schedule.getStartAt().isAfter( schedule.getEndAt() ) ) {
            throw new IllegalArgumentException("startAt must be before endAt");
        }
        if ( !schedule.getGroup().getCourses().contains( schedule.getCourse() ) ) {
            throw new IllegalArgumentException("group has not such course with courseId = " + schedule.getCourse().getId() );
        }
    }

    public Schedule update(Schedule entity) {
        validate( entity );
        return super.update( entity );
    }
}
