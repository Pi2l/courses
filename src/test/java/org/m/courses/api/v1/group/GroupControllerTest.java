package org.m.courses.api.v1.group;

import org.junit.jupiter.api.BeforeEach;
import org.m.courses.api.v1.common.AbstractControllerTest;
import org.m.courses.api.v1.controller.group.GroupController;
import org.m.courses.api.v1.controller.group.GroupRequest;
import org.m.courses.api.v1.controller.group.GroupResponse;
import org.m.courses.builder.CourseBuilder;
import org.m.courses.builder.GroupBuilder;
import org.m.courses.exception.AccessDeniedException;
import org.m.courses.filtering.GroupSpecificationsBuilder;
import org.m.courses.filtering.SearchCriteria;
import org.m.courses.model.Course;
import org.m.courses.model.Group;
import org.m.courses.service.CourseService;
import org.m.courses.service.GroupService;
import org.m.courses.service.UserService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.m.courses.api.v1.controller.common.ApiPath.GROUP_API;
import static org.m.courses.filtering.FilteringOperation.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest( GroupController.class )
public class GroupControllerTest extends AbstractControllerTest<Group, GroupRequest, GroupResponse> {

    @MockBean
    private GroupService groupService;

    @MockBean
    private UserService userService;

    @MockBean
    private CourseService courseService;

    @SpyBean
    private GroupSpecificationsBuilder groupSpecificationsBuilder;

    @BeforeEach
    void init() {
        when(courseService.get( anyLong() ))
                .thenAnswer(answer -> CourseBuilder.builder().setId( answer.getArgument(0) ).build() );
    }

    @Override
    protected String getControllerPath() {
        return GROUP_API;
    }

    @Override
    protected Class<Group> getEntityClass() {
        return Group.class;
    }

    @Override
    protected Group getNewEntity() {
        return GroupBuilder
                .builder()
                .setCourses(Set.of(CourseBuilder.builder().build(), CourseBuilder.builder().build()))
                .build();
    }

    @Override
    protected GroupResponse convertToResponse(Group entity) {
        return new GroupResponse( entity );
    }

    @Override
    protected GroupRequest convertToRequest(Group entity) {
        GroupRequest request = new GroupRequest();
        request.setName( entity.getName() );

        request.setCourseIds( entity.getCourses().stream().map(Course::getId).collect(Collectors.toSet()) );
        return request;
    }

    @Override
    protected List<Function<Group, Object>> getValueToBeUpdated() {
        List< Function<Group, Object> > valuesToBeUpdated = new ArrayList<>();

        valuesToBeUpdated.add(Group::getName);
        return valuesToBeUpdated;
    }

    @Override
    protected Map<Pair<Consumer<GroupRequest>, Runnable>, Pair<String, String>> getCreateWithWrongValuesTestParameters() {
        Map< Pair<Consumer<GroupRequest>, Runnable>, Pair<String, String>> wrongValues = new HashMap<>();

        setupWrongValues(wrongValues);

        return wrongValues;
    }

    private void setupWrongValues(Map<Pair<Consumer<GroupRequest>, Runnable>, Pair<String, String>> wrongValues) {
        wrongValues.put( Pair.of( req -> req.setName("   "), () -> {}), Pair.of("name", "must not be blank") );
        wrongValues.put( Pair.of( req -> req.setName(null), () -> {}), Pair.of("name", "must not be blank") );
        wrongValues.put( Pair.of( req -> req.setName(""), () -> {}), Pair.of("name", "must not be blank") );
    }

    @Override
    protected Map<Pair<Consumer<GroupRequest>, Runnable>, Pair<String, String>> getUpdateWithWrongValuesTestParameters() {
        Map<Pair<Consumer<GroupRequest>, Runnable>, Pair<String, String>> wrongValues = new HashMap<>();

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
        when(groupService.get( anyLong() )).thenReturn( getNewEntity() );
        doThrow(exception)
                .when(groupService).update(any(Group.class));
        doThrow(exception)
                .when(groupService).create(any(Group.class));
    }

    @Override
    protected Map<Pair<Runnable, ResultMatcher>, Pair<String, Supplier<Object>>> getUpdateServiceIllegalArgumentExceptionTest() {
        return getCreateServiceIllegalArgumentExceptionTest();
    }

    @Override
    protected Map<Map<String, Object>, Pair<Function<Group, Object>, Supplier<Object>>> getPatchValuesTestParameters() {
        Map<Map<String, Object>, Pair<Function<Group, Object>, Supplier<Object>>> map = new HashMap<>();

        map.put(
                Map.of("name", "name1"),
                Pair.of( Group::getName, () -> "name1" ) );

        Course course1 = CourseBuilder.builder().setId(1L).build();
        Course course2 = CourseBuilder.builder().setId(2L).build();
        map.put(
                Map.of("courseIds", "" + course1.getId() + "," + course2.getId() ),
                Pair.of( Group::getCourses, () -> Set.of( course1, course2 ) ) );

        return map;
    }

    @Override
    protected Map<Map<String, Object>, Pair<String, Object> > getPatchInvalidValuesTestParameters() {
        Map<Map<String, Object>, Pair<String, Object>> map = new HashMap<>();

        getPatchInvalidValues(map);

        return map;
    }

    private void getPatchInvalidValues(Map<Map<String, Object>, Pair<String, Object>> map) {
        setupBlankField(map, "name");
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
    protected Map< Consumer< GroupRequest >, Pair< Function<Group, Object>, Object> > getCreateWithOptionalValuesTestParameters() {
        return new HashMap<>();
    }

    @Override
    protected Map<Consumer<GroupRequest>, Pair<Function<Group, Object>, Object>> getUpdateWithOptionalValuesTestParameters() {
        return getCreateWithOptionalValuesTestParameters();
    }

    @Override
    protected Map< List<String>, Sort > getSortingTestParams() {
        Map< List<String>, Sort > map = new HashMap<>();

        map.put( List.of("name"), Sort.by(Sort.Direction.ASC, "name") );
        map.put( List.of("name,desc"), Sort.by(Sort.Direction.DESC, "name") );

        return map;
    }

    @Override
    protected Map< List<String>, List<SearchCriteria> > getFilteringTestParams() {
        Map< List<String>, List<SearchCriteria> > map = new HashMap<>();

        map.put(List.of("name=name1", "name!=name2", "name:name3"),
                        List.of(
                            new SearchCriteria("name", EQUAL, "name1"),
                            new SearchCriteria("name", NOT_EQUAL, "name2"),
                            new SearchCriteria("name", CONTAIN, "name3") ) );
        return map;
    }

    @Override
    protected Map< String, String > getInvalidFilteringTestParams() {
        Map< String, String > map = new HashMap<>();

        map.put("fn=firstName1", "Operation 'EQUAL' is not supported for property firstName");

        return map;
    }

    @Override
    protected GroupSpecificationsBuilder getEntitySpecificationsBuilder() {
        return groupSpecificationsBuilder;
    }

    @Override
    protected GroupService getService() {
        return groupService;
    }
}
