package org.m.courses.service;

import org.m.courses.dao.AbstractDao;
import org.m.courses.dao.GroupDao;
import org.m.courses.model.Group;
import org.springframework.stereotype.Service;

@Service
public class GroupService extends AbstractService<Group> {

    private GroupDao groupDao;

    public GroupService(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    @Override
    protected AbstractDao<Group> getDao() {
        return groupDao;
    }

}
