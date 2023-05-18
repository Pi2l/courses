package org.m.courses.dao;

import org.m.courses.builder.RefreshTokenBuilder;
import org.m.courses.model.RefreshToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
public class RefreshTokenDaoTest extends AbstractDaoTest<RefreshToken>  {

    @Autowired
    private RefreshTokenDao refreshTokenDao;

    @Autowired
    private RefreshTokenBuilder refreshTokenBuilder;

    protected AbstractDao<RefreshToken> getDao() {
        return refreshTokenDao;
    }

    @Override
    protected RefreshToken entityToDB() {
        return refreshTokenBuilder.toDB();
    }

    @Override
    protected RefreshToken buildEntity() {
        return refreshTokenBuilder.build();
    }

    @Override
    protected RefreshToken buildNewEntity() {
        return refreshTokenBuilder.buildNew();
    }

    @Override
    protected void assertEntitiesEqual(RefreshToken updatedEntity, RefreshToken entityFromDB) {
        assertEquals(updatedEntity.getToken(), entityFromDB.getToken());
        assertEquals(updatedEntity.getLogin(), entityFromDB.getLogin());
    }
}


