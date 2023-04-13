package org.m.courses.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.m.courses.model.Identity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractDaoTest<Entity extends Identity<Long>> extends Autologinable {

    protected abstract AbstractDao<Entity> getDao();

    protected abstract Entity entityToDB();

    protected abstract Entity buildEntity();

    protected abstract Entity buildNewEntity();

    protected abstract void assertEntitiesEqual(Entity e1, Entity e2);

    @AfterEach
    void cleanDB() {
        getDao().getAll().forEach( course -> getDao().delete(course.getId() ) );
    }

    @Test
    public void getAllEntitiesTest() {
        clearDB();

        entityToDB();
        entityToDB();

        List<Entity> entities = getDao().getAll();

        assertEquals(2, entities.size());
    }

    private void clearDB() {
        getDao().getAll().forEach(entity -> getDao().delete( entity.getId() ) );
    }

    @Test
    public void getEntityTest() {
        Entity entity = entityToDB();
        Entity entityFromDB = getDao().get(entity.getId());

        assertEquals(entity, entityFromDB);
    }

    @Test
    public void saveEntityTest() {
        Entity entity = buildNewEntity();

        getDao().create(entity);

        assertNotNull( getDao().get(entity.getId()) );
    }
    @Test
    public void updateEntityTest() {
        Entity entity = entityToDB();
        Entity updatedEntity = buildEntity();
        updatedEntity.setId( entity.getId() );

        Entity entityFromDB = getDao().update(updatedEntity);
        assertNotNull(entityFromDB);

        assertEntitiesEqual(updatedEntity, entityFromDB);
    }

    @Test
    public void deleteEntityTest() {
        Entity entityToDelete = entityToDB();

        getDao().delete( entityToDelete.getId() );

        assertNull( getDao().get(entityToDelete.getId()) );

    }
}
