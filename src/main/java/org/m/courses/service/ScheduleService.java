package org.m.courses.service;

import org.m.courses.dao.AbstractDao;
import org.m.courses.dao.ScheduleDao;
import org.m.courses.model.Schedule;
import org.springframework.stereotype.Service;

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
    
}
