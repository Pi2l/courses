package org.m.courses.dao;

import org.junit.jupiter.api.Test;
import org.m.courses.auth.AuthManager;
import org.m.courses.builder.RefreshTokenBuilder;
import org.m.courses.model.RefreshToken;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


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

    @Test
    void userShouldModifyItsRefreshTokenTest() {
        User user = userBuilder.setRole(Role.USER).toDB();
        User otherUser = userBuilder.setRole(Role.USER).toDB();
        long notExistingRefreshTokenId = 23L;
        AuthManager.loginAs( user );

        RefreshToken refreshTokenOfUser = refreshTokenBuilder.setLogin(user.getLogin()).toDB();
        RefreshToken refreshTokenOfOtherUser = refreshTokenBuilder.setLogin(otherUser.getLogin()).toDB();

        assertTrue( refreshTokenDao.canModify(refreshTokenOfUser.getId()) );
        assertFalse( refreshTokenDao.canModify(refreshTokenOfOtherUser.getId()) );
        assertFalse( refreshTokenDao.canModify(notExistingRefreshTokenId) );
    }

    @Test
    void adminShouldModifyAnyRefreshTokenTest() {
        User admin = userBuilder.setRole(Role.ADMIN).toDB();
        User otherUser = userBuilder.setRole(Role.USER).toDB();
        long notExistingRefreshTokenId = 23L;
        AuthManager.loginAs( admin );

        RefreshToken refreshTokenOfAdmin = refreshTokenBuilder.setLogin(admin.getLogin()).toDB();
        RefreshToken refreshTokenOfOtherUser = refreshTokenBuilder.setLogin(otherUser.getLogin()).toDB();

        assertTrue( refreshTokenDao.canModify(refreshTokenOfAdmin.getId()) );
        assertTrue( refreshTokenDao.canModify(refreshTokenOfOtherUser.getId()) );
        assertTrue( refreshTokenDao.canModify(notExistingRefreshTokenId) );
    }
}


