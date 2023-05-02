package org.m.courses.api.v1.user;

import org.junit.jupiter.api.BeforeEach;
import org.m.courses.api.v1.common.AbstractControllerTest;
import org.m.courses.api.v1.controller.user.UserController;
import org.m.courses.api.v1.controller.user.UserRequest;
import org.m.courses.api.v1.controller.user.UserResponse;
import org.m.courses.builder.GroupBuilder;
import org.m.courses.builder.UserBuilder;
import org.m.courses.exception.AccessDeniedException;
import org.m.courses.filtering.SearchCriteria;
import org.m.courses.filtering.UserSpecificationsBuilder;
import org.m.courses.model.Group;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.m.courses.service.GroupService;
import org.m.courses.service.UserService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.m.courses.api.v1.controller.common.ApiPath.USER_API;
import static org.m.courses.filtering.FilteringOperation.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest( UserController.class )
public class UserControllerTest extends AbstractControllerTest<User, UserRequest, UserResponse> {

    @MockBean
    private UserService userService;

    @MockBean
    private GroupService groupService;

    @SpyBean
    private UserSpecificationsBuilder userEntitySpecificationsBuilder;

    @BeforeEach
    private void init() {
        mockIsUnique(true);
        mockGetGroup();
    }

    private void mockIsUnique(boolean value) {
        when( getService().isUnique( any( getEntityClass() ) ) ).thenReturn(value);
    }

    private void mockGetGroup() {
        when( groupService.get( anyLong() ) )
                .thenAnswer( answer -> GroupBuilder.builder().setId( answer.getArgument(0) ).build() );
    }

    @Override
    protected String getControllerPath() {
        return USER_API;
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    protected User getNewEntity() {
        return UserBuilder.builder()
                .setGroup(GroupBuilder.builder().build())
                .build();
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
        valuesToBeUpdated.add(User::getGroup);

        return valuesToBeUpdated;
    }

    @Override
    protected Map<Pair<Consumer<UserRequest>, Runnable>, Pair<String, String>> getCreateWithWrongValuesTestParameters() {
        Map<Pair<Consumer<UserRequest>, Runnable>, Pair<String, String>> wrongValues = new HashMap<>();

        setupWrongValues(wrongValues);
        wrongValues.put(Pair.of(userRequest -> userRequest.setPassword(null), () -> {}), Pair.of("password", "must not be blank") );
        wrongValues.put(Pair.of(userRequest -> userRequest.setPassword(""), () -> {}), Pair.of("password", "must not be blank") );
        wrongValues.put(Pair.of(userRequest -> userRequest.setPassword("   "), () -> {}), Pair.of("password", "must not be blank") );

        return wrongValues;
    }

    private void setupWrongValues(Map<Pair<Consumer<UserRequest>, Runnable>, Pair<String, String>> wrongValues) {
        wrongValues.put(
            Pair.of( userRequest -> userRequest.setFirstName(null), () -> {}),
            Pair.of("firstName", "must not be blank") );
        wrongValues.put(
            Pair.of( userRequest -> userRequest.setFirstName(""), () -> {}),
            Pair.of("firstName", "must not be blank") );
        wrongValues.put(
            Pair.of( userRequest -> userRequest.setFirstName("   "), () -> {}),
            Pair.of("firstName", "must not be blank") );

        wrongValues.put(
            Pair.of( userRequest -> userRequest.setLastName(null), () -> {}),
            Pair.of("lastName", "must not be blank") );
        wrongValues.put(
            Pair.of( userRequest -> userRequest.setLastName(""), () -> {}),
            Pair.of("lastName", "must not be blank") );
        wrongValues.put(
            Pair.of( userRequest -> userRequest.setLastName("   "), () -> {}),
            Pair.of("lastName", "must not be blank") );

        wrongValues.put(
            Pair.of( userRequest -> userRequest.setPhoneNumber(null), () -> {}),
            Pair.of("phoneNumber", "must not be blank") );
        wrongValues.put(
            Pair.of( userRequest -> userRequest.setPhoneNumber(""), () -> {}),
            Pair.of("phoneNumber", "must not be blank") );
        wrongValues.put(
            Pair.of( userRequest -> userRequest.setPhoneNumber("   "), () -> {}),
            Pair.of("phoneNumber", "must not be blank") );
        wrongValues.put(
            Pair.of( userRequest -> userRequest.setPhoneNumber("12345678901234567890123456789012345678901"), () -> {}),
            Pair.of("phoneNumber", "size must be between 0 and 20") );

        wrongValues.put(
            Pair.of( userRequest -> userRequest.setLogin(null), () -> {}),
            Pair.of("login", "must not be blank") );
        wrongValues.put(
            Pair.of(
                    userRequest -> {
                        mockIsUnique(false);
                        userRequest.setLogin("login");
                    },
                    () -> mockIsUnique(true) ),
            Pair.of("login", "must be unique") );//unqinue

        wrongValues.put(
            Pair.of( userRequest -> userRequest.setRole(null), () -> {}),
            Pair.of("role", "must not be null") );

        Group group = GroupBuilder.builder().build();
        wrongValues.put(
            Pair.of(
                    request -> {
                        when( groupService.get( anyLong() ) ).thenReturn( null );
                        request.setGroupId(group.getId());
                    }, this::mockGetGroup),
                Pair.of( "cause", "group not found with id = " + group.getId() ) );
    }

    @Override
    protected Map<Pair<Consumer<UserRequest>, Runnable>, Pair<String, String>> getUpdateWithWrongValuesTestParameters() {
        Map<Pair<Consumer<UserRequest>, Runnable>, Pair<String, String>> wrongValues = new HashMap<>();

        setupWrongValues(wrongValues);

        return wrongValues;
    }

    @Override
    protected Map< Pair<Runnable, ResultMatcher>, Pair<String, Supplier<Object>> > getCreateServiceIllegalArgumentExceptionTest() {
        Map< Pair<Runnable, ResultMatcher>, Pair<String, Supplier<Object>> > map = new HashMap<>();

        map.put(
                Pair.of( () -> getCreateOrUpdateThrow(new AccessDeniedException()), status().isForbidden() ),
                Pair.of( "cause", () -> null ) );
        map.put(
                Pair.of( () -> getCreateOrUpdateThrow(new IllegalArgumentException("entity cannot be null")), status().isBadRequest() ),
                Pair.of( "cause", () -> "entity cannot be null" ) );
        return map;
    }

    private void getCreateOrUpdateThrow(Exception exception) {
        when(userService.get( anyLong() )).thenReturn( getNewEntity() );
        doThrow(exception)
                .when(userService).update(any(User.class));
        doThrow(exception)
                .when(userService).create(any(User.class));
    }

    @Override
    protected Map<Pair<Runnable, ResultMatcher>, Pair<String, Supplier<Object>>> getUpdateServiceIllegalArgumentExceptionTest() {
        return getCreateServiceIllegalArgumentExceptionTest();
    }

    @Override
    protected Map<Map<String, Object>, Pair<Function<User, Object>, Supplier<Object>>> getPatchValuesTestParameters() {
        Map<Map<String, Object>, Pair<Function<User, Object>, Supplier<Object>>> map = new HashMap<>();

        map.put(
                Map.of("firstName", "firstName1"),
                Pair.of( User::getFirstName, () -> "firstName1" ) );

        map.put(
                Map.of("lastName", "lastName1"),
                Pair.of( User::getLastName, () -> "lastName1" ) );

        map.put(
                Map.of("phoneNumber", "phoneNumber1"),
                Pair.of( User::getPhoneNumber, () -> "phoneNumber1" ) );

        map.put(
                Map.of("login", "login1"),
                Pair.of( User::getLogin, () -> "login1" ) );

        map.put(
                Map.of("password", "password1"),
                Pair.of( User::getPassword, () -> "password1" ) );

        map.put(
                Map.of("role", Role.TEACHER),
                Pair.of( User::getRole, () -> Role.TEACHER ) );

        Group group = GroupBuilder.builder().build();
        map.put(
                Map.of("groupId", group.getId().toString()),
                Pair.of( User::getGroup, () -> group ) );
        return map;
    }

    @Override
    protected Map<Map<String, Object>, Pair<String, Object> > getPatchInvalidValuesTestParameters() {
        Map<Map<String, Object>, Pair<String, Object>> map = new HashMap<>();
        mockIsUnique(false);

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

        when( groupService.get( anyLong() ) ).thenReturn( null );
        Group group = GroupBuilder.builder().build();
        map.put(
                Map.of("groupId", group.getId()),
                Pair.of( "cause", "group not found with id = " + group.getId() ) );
    }

    private void setupBlankField(Map<Map<String, Object>, Pair<String, Object>> map, String fieldName) {
        Map<String, Object> fieldMap = new HashMap<>();
        map.put(
                Map.of(fieldName, ""),
                Pair.of( fieldName, "must not be blank" ) );
        map.put(
                Map.of(fieldName, "   "),
                Pair.of( fieldName, "must not be blank" ) );
        fieldMap.put(fieldName, null);
        map.put(
                fieldMap,
                Pair.of( fieldName, "must not be blank" ) );
    }

    @Override
    protected Map< Consumer< UserRequest >, Pair< Function<User, Object>, Object> > getCreateWithOptionalValuesTestParameters() {
        Map<Consumer<UserRequest>, Pair<Function<User, Object>, Object>> optionalValues = new HashMap<>();

        return optionalValues;
    }

    @Override
    protected Map<Consumer<UserRequest>, Pair<Function<User, Object>, Object>> getUpdateWithOptionalValuesTestParameters() {
        return getCreateWithOptionalValuesTestParameters();
    }

    @Override
    protected Map< List<String>, Sort > getSortingTestParams() {
        Map< List<String>, Sort > map = new HashMap<>();

        map.put( List.of("firstName"), Sort.by(Sort.Direction.ASC, "firstName") );
        map.put( List.of("firstName,desc"), Sort.by(Sort.Direction.DESC, "firstName") );

        map.put( List.of("latName"), Sort.by(Sort.Direction.ASC, "latName") );
        map.put( List.of("latName,desc"), Sort.by(Sort.Direction.DESC, "latName") );

        map.put( List.of("phoneNumber"), Sort.by(Sort.Direction.ASC, "phoneNumber") );
        map.put( List.of("phoneNumber,desc"), Sort.by(Sort.Direction.DESC, "phoneNumber") );
        
        map.put( List.of("login"), Sort.by(Sort.Direction.ASC, "login") );
        map.put( List.of("login,desc"), Sort.by(Sort.Direction.DESC, "login") );

        map.put( List.of("role"), Sort.by(Sort.Direction.ASC, "role") );
        map.put( List.of("role,desc"), Sort.by(Sort.Direction.DESC, "role") );

        map.put( List.of("groupId"), Sort.by(Sort.Direction.ASC, "groupId") );
        map.put( List.of("groupId,desc"), Sort.by(Sort.Direction.DESC, "groupId") );

        return map;
    }

    @Override
    protected Map< List<String>, List<SearchCriteria> > getFilteringTestParams() {
        Map< List<String>, List<SearchCriteria> > map = new HashMap<>();

        map.put(List.of("firstName=firstName1", "firstName!=firstName2"),
                        List.of(
                            new SearchCriteria("firstName", EQUAL, "firstName1"),
                            new SearchCriteria("firstName", NOT_EQUAL, "firstName2") ) );

        map.put(List.of("lastName=lastName1", "lastName!=lastName2"),
                        List.of(
                            new SearchCriteria("lastName", EQUAL, "lastName1"),
                            new SearchCriteria("lastName", NOT_EQUAL, "lastName2") ) );

        map.put(List.of("phoneNumber=phoneNumber1", "phoneNumber!=phoneNumber2"),
                        List.of(
                            new SearchCriteria("phoneNumber", EQUAL, "phoneNumber1"),
                            new SearchCriteria("phoneNumber", NOT_EQUAL, "phoneNumber2") ) );

        map.put(List.of("login=login1", "login!=login2"),
                        List.of(
                            new SearchCriteria("login", EQUAL, "login1"),
                            new SearchCriteria("login", NOT_EQUAL, "login2") ) );

        map.put(List.of("role=USER", "role!=ADMIN"),
                        List.of(
                            new SearchCriteria("role", EQUAL, Role.USER),
                            new SearchCriteria("role", NOT_EQUAL, Role.ADMIN) ) );

        map.put(List.of("groupId=1", "groupId!=2", "groupId=null", "groupId!=null"),
                List.of(
                        new SearchCriteria("groupId", EQUAL, 1L),
                        new SearchCriteria("groupId", NOT_EQUAL, 2L),
                        new SearchCriteria("groupId", EQUAL, null),
                        new SearchCriteria("groupId", NOT_EQUAL, null)) );
        return map;
    }

    @Override
    protected Map< String, String > getInvalidFilteringTestParams() {
        Map< String, String > map = new HashMap<>();

        map.put("fn=firstName1", "Operation 'EQUAL' is not supported for property firstName");

        return map;
    }

    @Override
    protected UserSpecificationsBuilder getEntitySpecificationsBuilder() {
        return userEntitySpecificationsBuilder;
    }

    @Override
    protected UserService getService() {
        return userService;
    }
}
