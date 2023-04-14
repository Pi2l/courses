package org.m.courses.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.m.courses.auth.AuthManager;
import org.m.courses.builder.UserBuilder;
import org.m.courses.dao.Autologinable;
import org.m.courses.model.Identity;
import org.m.courses.model.Role;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractServiceTest<Entity extends Identity<Long>> extends Autologinable {

    @Autowired protected UserBuilder userBuilder;

    @AfterEach
    void clearDB() {
        AuthManager.loginAs( userBuilder.setRole(Role.ADMIN).build() );
        getService().getAll().forEach(user -> getService().delete( user.getId() ) );
    }

    @Test
    public void getEntityTest() {
        Entity entity = buildNewEntity();
        getService().create( entity );

        assertNotNull( getService().get( entity.getId() ) );
    }

    @Test
    public void getAllEntitiesTest() {
        getService().create( buildNewEntity() );
        getService().create( buildNewEntity() );

        assertEquals( 2, getService().getAll().size() );
    }

    @Test
    public void createEntityTest() {
        Entity entity = buildNewEntity();

        Entity createdEntity = getService().create( entity );

        assertNotNull( getService().get( entity.getId() ) );
        assertEquals( createdEntity, entity );
    }

    @Test
    public void updateEntityTest() {
        Entity entity = getService().create( buildNewEntity() );
        Entity updatedEntity = buildEntity();
        updatedEntity.setId( entity.getId() );

        Entity entityFromDB = getService().update( updatedEntity );

        assertEntitiesEqual(updatedEntity, entityFromDB);
    }

    @Test
    public void deleteEntityTest() {
        Entity entity = getService().create( buildNewEntity() );

        getService().delete( entity.getId() );

        assertNull(getService().get( entity.getId() ));
    }


    protected abstract AbstractService<Entity> getService();

    protected abstract Entity entityToDB();

    protected abstract Entity buildEntity();

    protected abstract Entity buildNewEntity();

    protected abstract void assertEntitiesEqual(Entity e1, Entity e2);
}
