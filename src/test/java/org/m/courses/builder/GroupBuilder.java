package org.m.courses.builder;

import org.m.courses.dao.GroupDao;
import org.m.courses.model.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;


@Component
public class GroupBuilder {

    @Autowired private GroupDao groupDao;
    private Group group;

    public static GroupBuilder builder() {
        return new GroupBuilder();
    }

    public GroupBuilder() {
        initDefaultUser();
    }

    public Group build() {
        Group odlEntity = group;
        initDefaultUser();
        return odlEntity;
    }

    public Group buildNew() {
        return setId(null)
                .build();
    }

    // Spring based
    public Group toDB() {
        return groupDao.create( buildNew() );
    }

    private GroupBuilder initDefaultUser() {
        long randomValue = Math.abs(new SecureRandom().nextLong()) % 1000000;
        this.group = new Group();

        setId(randomValue);
        setName( "Group_" + randomValue );
        return this;
    }

    public GroupBuilder setId(Long id) {
        group.setId(id);
        return this;
    }

    public GroupBuilder setName(String name) {
        group.setName(name);
        return this;
    }
}
