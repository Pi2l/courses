package org.m.courses.dao;

import org.m.courses.builder.GroupBuilder;
import org.m.courses.builder.UserBuilder;
import org.m.courses.model.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
public class GroupDaoTest extends AbstractDaoTest<Group>  {

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private GroupBuilder groupBuilder;

    protected AbstractDao<Group> getDao() {
        return groupDao;
    }

    @Override
    protected Group entityToDB() {
        return groupBuilder.toDB();
    }

    @Override
    protected Group buildEntity() {
        return groupBuilder.build();
    }

    @Override
    protected Group buildNewEntity() {
        return groupBuilder.buildNew();
    }

    @Override
    protected void assertEntitiesEqual(Group updatedEntity, Group entityFromDB) {
        assertEquals(updatedEntity.getName(), entityFromDB.getName());

        assertTrue( updatedEntity.getUsers().containsAll( entityFromDB.getUsers()) );
        assertTrue( entityFromDB.getUsers().containsAll( updatedEntity.getUsers()) );
    }
}

