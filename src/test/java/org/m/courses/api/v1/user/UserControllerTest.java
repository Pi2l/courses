package org.m.courses.api.v1.user;

import org.junit.jupiter.api.Test;
import org.m.courses.api.v1.common.AbstractControllerTest;
import org.m.courses.api.v1.controller.user.UserRequest;
import org.m.courses.api.v1.controller.user.UserResponse;
import org.m.courses.builder.UserBuilder;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.m.courses.service.UserService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class UserControllerTest extends AbstractControllerTest<User, UserRequest, UserResponse> {

    @MockBean
    private UserService userService;

    @Test
    void getTest() throws Exception {
        when(userService.get( anyLong() ))
                .thenReturn( UserBuilder.builder().buildNew() );

        mockMvc.perform(get( getControllerPath() ))
                .andDo(print())
                .andExpect( status().isOk() );
    }

    @Override
    protected String getControllerPath() {
        return "/api/v1/users/";
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    protected User getNewEntity() {
        return UserBuilder.builder().build();
    }

    @Override
    protected UserResponse convertToResponse(User user) {
        return new UserResponse( user );
    }

    @Override
    protected UserRequest convertToRequest(User user) {
        return new UserRequest( user );
    }

    @Override
    protected List<Function<User, Object>> getValueToBeUpdated() {
        List<Function<User, Object>> valuesToBeUpdated = new ArrayList<>();

        valuesToBeUpdated.add(User::getFirstName);
        valuesToBeUpdated.add(User::getLastName);
        valuesToBeUpdated.add(User::getPhoneNumber);
        valuesToBeUpdated.add(User::getLogin);
        valuesToBeUpdated.add(User::getPassword);
        valuesToBeUpdated.add(User::getRole);
//        Roles?

        return valuesToBeUpdated;
    }

    @Override
    protected Map<Consumer<UserRequest>, Pair<String, String>> getCreateWithWrongValuesTestParameters() {
        Map<Consumer<UserRequest>, Pair<String, String>> wrongValues = new HashMap<>();

        setupWrongValues(wrongValues);
        wrongValues.put(userRequest -> userRequest.setPassword(null), Pair.of("password", "must not be blank") );
        wrongValues.put(userRequest -> userRequest.setPassword(""), Pair.of("password", "must not be blank") );
        wrongValues.put(userRequest -> userRequest.setPassword("   "), Pair.of("password", "must not be blank") );

        return wrongValues;
    }

    private void setupWrongValues(Map<Consumer<UserRequest>, Pair<String, String>> wrongValues) {
        wrongValues.put(
            userRequest -> userRequest.setFirstName(null),
            Pair.of("firstName", "must not be blank") );
        wrongValues.put(
            userRequest -> userRequest.setFirstName(""),
            Pair.of("firstName", "must not be blank") );
        wrongValues.put(
            userRequest -> userRequest.setFirstName("   "),
            Pair.of("firstName", "must not be blank") );

        wrongValues.put(
            userRequest -> userRequest.setLastName(null),
            Pair.of("lastName", "must not be blank") );
        wrongValues.put(
            userRequest -> userRequest.setLastName(""),
            Pair.of("lastName", "must not be blank") );
        wrongValues.put(
            userRequest -> userRequest.setLastName("   "),
            Pair.of("lastName", "must not be blank") );

        wrongValues.put(
            userRequest -> userRequest.setPhoneNumber(null),
            Pair.of("phoneNumber", "must not be blank") );
        wrongValues.put(
            userRequest -> userRequest.setPhoneNumber(""),
            Pair.of("phoneNumber", "must not be blank") );
        wrongValues.put(
            userRequest -> userRequest.setPhoneNumber("   "),
            Pair.of("phoneNumber", "must not be blank") );
        wrongValues.put(
            userRequest -> userRequest.setPhoneNumber("12345678901234567890123456789012345678901"),
            Pair.of("phoneNumber", "size must be between 0 and 20") );

        wrongValues.put(
            userRequest -> userRequest.setLogin(null),
            Pair.of("login", "must not be blank") );
        wrongValues.put(
            userRequest -> {
                when(getService().isUnique( any( getEntityClass() ) )).thenReturn(false);
                userRequest.setLogin("login");
            },
            Pair.of("login", "must be unique") );//unqinue

        wrongValues.put(
            userRequest -> userRequest.setRole(null),
            Pair.of("role", "must not be null") );
    }

    @Override
    protected Map<Consumer<UserRequest>, Pair<String, String>> getUpdateWithWrongValuesTestParameters() {
        Map<Consumer<UserRequest>, Pair<String, String>> wrongValues = new HashMap<>();

        setupWrongValues(wrongValues);

        return wrongValues;
    }

    @Override
    protected Map<Map<String, Object>, Pair<Function<User, Object>, Object>> getPatchValuesTestParameters() {
        Map<Map<String, Object>, Pair<Function<User, Object>, Object>> map = new HashMap<>();
        when(getService().isUnique( any( getEntityClass() ) )).thenReturn(true);

        map.put(
                Map.of("firstName", "firstName1"),
                Pair.of( User::getFirstName, "firstName1" ) );

        map.put(
                Map.of("lastName", "lastName1"),
                Pair.of( User::getLastName, "lastName1" ) );

        map.put(
                Map.of("phoneNumber", "phoneNumber1"),
                Pair.of( User::getPhoneNumber, "phoneNumber1" ) );

        map.put(
                Map.of("login", "login1"),
                Pair.of( User::getLogin, "login1" ) );

        map.put(
                Map.of("password", "password1"),
                Pair.of( User::getPassword, "password1" ) );

        map.put(
                Map.of("role", Role.TEACHER),
                Pair.of( User::getRole, Role.TEACHER ) );

        return map;
    }

    @Override
    protected Map<Map<String, Object>, Pair<String, Object> > getPatchInvalidValuesTestParameters() {
        Map<Map<String, Object>, Pair<String, Object>> map = new HashMap<>();
        when(getService().isUnique( any( getEntityClass() ) )).thenReturn(false);

        getPatchInvalidValues(map);

        return map;
    }

    private void getPatchInvalidValues(Map<Map<String, Object>, Pair<String, Object>> map) {
        setupBlankField(map, "firstName");
        setupBlankField(map, "lastName");
        setupBlankField(map, "phoneNumber");
        setupBlankField(map, "login");
        setupBlankField(map, "password");

        map.put(
                Map.of("phoneNumber", "12345678901234567890123456789012345678901"),
                Pair.of( "phoneNumber", "size must be between 0 and 20" ) );

        map.put(
                Map.of("login", "notUniqueLogin"),//unqinue
                Pair.of( "login", "must be unique" ) );

        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("role", null);
        map.put(
                roleMap,
                Pair.of( "role", "must not be null" ) );
    }

    private void setupBlankField(Map<Map<String, Object>, Pair<String, Object>> map, String fieldName) {
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put(fieldName, null);
        map.put(
                Map.of(fieldName, ""),
                Pair.of( fieldName, "must not be blank" ) );
        map.put(
                Map.of(fieldName, "   "),
                Pair.of( fieldName, "must not be blank" ) );
        map.put(
                fieldMap,
                Pair.of( fieldName, "must not be blank" ) );
    }

    @Test
    @Override
    public void updateEntity() throws Exception {
        when(getService().isUnique( any( getEntityClass() ) )).thenReturn(true);
        super.updateEntity();
    }

    @Test
    @Override
    public void createEntityTest() throws Exception {
        when(getService().isUnique( any( getEntityClass() ) )).thenReturn(true);
        super.createEntityTest();
    }


    @Override
    protected Map< Consumer< UserRequest >, Pair< Function<User, Object>, Object> > getCreateWithOptionalValuesTestParameters() {
        Map<Consumer<UserRequest>, Pair<Function<User, Object>, Object>> optionalValues = new HashMap<>();

        when(getService().isUnique( any( getEntityClass() ) )).thenReturn(true);

        return optionalValues;
    }

    @Override
    protected Map<Consumer<UserRequest>, Pair<Function<User, Object>, Object>> getUpdateWithOptionalValuesTestParameters() {
        return getCreateWithOptionalValuesTestParameters();
    }

    @Override
    protected UserService getService() {
        return userService;
    }
}
