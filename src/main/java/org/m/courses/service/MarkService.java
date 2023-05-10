package org.m.courses.service;

import org.m.courses.dao.AbstractDao;
import org.m.courses.dao.MarkDao;
import org.m.courses.dao.ScheduleDao;
import org.m.courses.model.Mark;
import org.m.courses.model.Schedule;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class MarkService extends AbstractService<Mark> {

    private MarkDao markDao;

    public MarkService(MarkDao markDao) {
        this.markDao = markDao;
    }

    @Override
    protected AbstractDao<Mark> getDao() {
        return markDao;
    }

    @Override
    public Mark create(Mark entity) {
        validate( entity );
        return super.create( entity );
    }

    private void validate(Mark mark) {
//        ZoneId zoneId = mark.getEndAt().getZone();
//
//        if ( mark.getEndAt().isBefore( ZonedDateTime.now(zoneId) ) ) {
//            throw new IllegalArgumentException("endAt must be after now");
//        }
//        if ( mark.getStartAt().isAfter( mark.getEndAt() ) ) {
//            throw new IllegalArgumentException("startAt must be before endAt");
//        }
//        if ( !mark.getGroup().getCourses().contains( mark.getCourse() ) ) {
//            throw new IllegalArgumentException("group has not such course with courseId = " + mark.getCourse().getId() );
//        }
    }

    public Mark update(Mark entity) {
        validate( entity );
        return super.update( entity );
    }
}
