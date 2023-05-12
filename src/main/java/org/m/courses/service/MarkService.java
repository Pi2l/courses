package org.m.courses.service;

import org.m.courses.dao.AbstractDao;
import org.m.courses.dao.MarkDao;
import org.m.courses.model.*;
import org.springframework.stereotype.Service;

import java.util.Set;

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
        User user = mark.getUser();
        if ( !user.getRole().equals( Role.USER ) ) {
            throw new IllegalArgumentException("user must have USER role");
        }

        Group group = user.getGroup();
        if ( group == null ) {
            throw new IllegalArgumentException("user has to be in group");
        }
        Set<Course> courses = group.getCourses();
        if ( courses == null ) {
            throw new IllegalArgumentException("group has contain any course");
        }
        if ( !courses.contains(mark.getCourse()) ) {
            throw new IllegalArgumentException("user does not belong to that course");
        }
    }

    public Mark update(Mark entity) {
        validate( entity );
        return super.update( entity );
    }
}
