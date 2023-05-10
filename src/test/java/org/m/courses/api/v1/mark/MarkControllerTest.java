package org.m.courses.api.v1.mark;

import org.junit.jupiter.api.BeforeEach;
import org.m.courses.api.v1.common.AbstractControllerTest;
import org.m.courses.api.v1.controller.mark.MarkController;
import org.m.courses.api.v1.controller.mark.MarkRequest;
import org.m.courses.api.v1.controller.mark.MarkResponse;
import org.m.courses.builder.CourseBuilder;
import org.m.courses.builder.MarkBuilder;
import org.m.courses.builder.UserBuilder;
import org.m.courses.exception.AccessDeniedException;
import org.m.courses.filtering.MarkSpecificationsBuilder;
import org.m.courses.filtering.SearchCriteria;
import org.m.courses.model.Course;
import org.m.courses.model.Mark;
import org.m.courses.model.User;
import org.m.courses.service.CourseService;
import org.m.courses.service.MarkService;
import org.m.courses.service.UserService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.m.courses.api.v1.controller.common.ApiPath.MARK_API;
import static org.m.courses.filtering.FilteringOperation.*;
import static org.m.courses.filtering.FilteringOperation.LESS_OR_EQUAL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest( MarkController.class )
public class MarkControllerTest extends AbstractControllerTest<Mark, MarkRequest, MarkResponse> {

    @MockBean
    private MarkService markService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private UserService userService;

    @SpyBean
    private MarkSpecificationsBuilder markSpecificationsBuilder;

    @BeforeEach
    void init() {
        mockGetCourse();
        mockGetUser();
    }

    private void mockGetUser() {
        when( userService.get( anyLong() ) )
                .thenAnswer( answer -> UserBuilder.builder().setId( answer.getArgument(0) ).build() );
    }

    private void mockGetCourse() {
        when( courseService.get( anyLong() ) )
                .thenAnswer( answer -> CourseBuilder.builder().setId( answer.getArgument(0) ).build() );
    }

    @Override
    protected String getControllerPath() {
        return MARK_API;
    }

    @Override
    protected Class<Mark> getEntityClass() {
        return Mark.class;
    }

    @Override
    protected Mark getNewEntity() {
        return MarkBuilder.builder()
                .setCourse( CourseBuilder.builder().build() )
                .setUser( UserBuilder.builder().build() )
                .build();
    }

    @Override
    protected MarkResponse convertToResponse(Mark entity) {
        return new MarkResponse( entity );
    }

    @Override
    protected MarkRequest convertToRequest(Mark entity) {
        MarkRequest request = new MarkRequest();
        request.setCourseId( entity.getCourse().getId() );
        request.setUserId( entity.getUser().getId() );
        request.setValue( entity.getValue() );
        return request;
    }

    @Override
    protected List<Function<Mark, Object>> getValueToBeUpdated() {
        List< Function<Mark, Object> > valuesToBeUpdated = new ArrayList<>();

        valuesToBeUpdated.add(Mark::getCourse);
        valuesToBeUpdated.add(Mark::getUser);
        valuesToBeUpdated.add(Mark::getValue);
        return valuesToBeUpdated;
    }

    @Override
    protected Map<Pair<Consumer<MarkRequest>, Runnable>, Pair<String, String>> getCreateWithWrongValuesTestParameters() {
        Map<Pair<Consumer<MarkRequest>, Runnable>, Pair<String, String>> wrongValues = new HashMap<>();

        setupWrongValues(wrongValues);

        return wrongValues;
    }

    private void setupWrongValues(Map< Pair<Consumer<MarkRequest>, Runnable>, Pair<String, String>> wrongValues) {
        wrongValues.put( Pair.of( req -> req.setCourseId(null), () -> {} ), Pair.of("courseId", "must not be null") );
        wrongValues.put( Pair.of( req -> req.setUserId(null), () -> {} ), Pair.of("userId", "must not be null") );

        Course course = CourseBuilder.builder().build();
        wrongValues.put(
                Pair.of( request -> {
                            when( courseService.get( anyLong() ) ).thenReturn( null );
                            request.setCourseId(course.getId());
                        }, this::mockGetCourse ),
                Pair.of( "cause", "course not found with id = " + course.getId() ) );

        User user = UserBuilder.builder().build();
        wrongValues.put(
                Pair.of( request -> {
                            when( userService.get( anyLong() ) ).thenReturn( null );
                            request.setUserId(user.getId());
                        }, this::mockGetUser ),
                Pair.of( "cause", "user not found with id = " + user.getId() ) );

        wrongValues.put(
                Pair.of(request -> request.setValue(-1), () -> {}),
                Pair.of("value", "must be between 0 and 100") );
        wrongValues.put(
                Pair.of(request -> request.setValue(101), () -> {}),
                Pair.of("value", "must be between 0 and 100") );
    }

    @Override
    protected Map< Pair<Consumer<MarkRequest>, Runnable>, Pair<String, String>> getUpdateWithWrongValuesTestParameters() {
        Map< Pair<Consumer<MarkRequest>, Runnable>, Pair<String, String>> wrongValues = new HashMap<>();

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

//        map.put(
//                Pair.of( () -> getCreateOrUpdateThrow(new IllegalArgumentException("group has not such course with courseId = 1")), status().isBadRequest() ),
//                Pair.of( "cause", () -> "group has not such course with courseId = 1") );
        return map;
    }

    private void getCreateOrUpdateThrow(Exception exception) {
        when(markService.get( anyLong() )).thenReturn( getNewEntity() );
        doThrow(exception)
                .when(markService).update( any(Mark.class) );
        doThrow(exception)
                .when(markService).create( any(Mark.class) );
    }

    @Override
    protected Map<Pair<Runnable, ResultMatcher>, Pair<String, Supplier<Object>>> getUpdateServiceIllegalArgumentExceptionTest() {
        return getCreateServiceIllegalArgumentExceptionTest();
    }

    @Override
    protected Map<Map<String, Object>, Pair<Function<Mark, Object>, Supplier<Object>>> getPatchValuesTestParameters() {
        Map<Map<String, Object>, Pair<Function<Mark, Object>, Supplier<Object>>> map = new HashMap<>();

        Course course = CourseBuilder.builder().build();
        map.put(
                Map.of("courseId", course.getId().toString()),
                Pair.of( Mark::getCourse, () -> course ) );

        User user = UserBuilder.builder().build();
        map.put(
                Map.of("userId", user.getId().toString()),
                Pair.of( Mark::getUser, () -> user ) );
        map.put(
                Map.of("value", "93"),
                Pair.of( Mark::getValue, () -> 93 ) );
        map.put(
                Map.of("value", ""),
                Pair.of( Mark::getValue, () -> null ) );
        return map;
    }

    @Override
    protected Map<Map<String, Object>, Pair<String, Object> > getPatchInvalidValuesTestParameters() {
        Map<Map<String, Object>, Pair<String, Object>> map = new LinkedHashMap<>();

        getPatchInvalidValues(map);

        getCreateOrUpdateReturnNull();
        Course course = CourseBuilder.builder().build();
        map.put(
                Map.of("courseId", course.getId()),
                Pair.of( "cause", "course not found with id = " + course.getId() ) );

        User user = UserBuilder.builder().build();
        map.put(
                Map.of("userId", user.getId()),
                Pair.of( "cause", "user not found with id = " + user.getId() ) );

        map.put(
                Map.of("value", -1),
                Pair.of("value", "must be between 0 and 100") );
        map.put(
                Map.of("value", 101),
                Pair.of("value", "must be between 0 and 100") );

        return map;
    }

    private void getCreateOrUpdateReturnNull() {
        when( courseService.get( anyLong() ) ).thenReturn( null );
        when( userService.get( anyLong() ) ).thenReturn( null );
    }

    private void getPatchInvalidValues(Map<Map<String, Object>, Pair<String, Object>> map) {
        setupNullField(map, "courseId");
        setupNullField(map, "userId");
    }

    private void setupNullField(Map<Map<String, Object>, Pair<String, Object>> map, String fieldName) {
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put(fieldName, null);
        map.put(
                fieldMap,
                Pair.of( fieldName, "must not be null" ) );
    }

    @Override
    protected Map< Consumer< MarkRequest >, Pair< Function<Mark, Object>, Object> > getCreateWithOptionalValuesTestParameters() {
        Map<Consumer<MarkRequest>, Pair<Function<Mark, Object>, Object>> optionalValues = new HashMap<>();

        return optionalValues;
    }

    @Override
    protected Map<Consumer<MarkRequest>, Pair<Function<Mark, Object>, Object>> getUpdateWithOptionalValuesTestParameters() {
        return getCreateWithOptionalValuesTestParameters();
    }

    @Override
    protected Map< List<String>, Sort > getSortingTestParams() {
        Map< List<String>, Sort > map = new HashMap<>();

        map.put( List.of("userId"), Sort.by(Sort.Direction.ASC, "userId") );
        map.put( List.of("userId,desc"), Sort.by(Sort.Direction.DESC, "userId") );

        map.put( List.of("courseId"), Sort.by(Sort.Direction.ASC, "courseId") );
        map.put( List.of("courseId,desc"), Sort.by(Sort.Direction.DESC, "courseId") );

        map.put( List.of("value"), Sort.by(Sort.Direction.ASC, "value") );
        map.put( List.of("value,desc"), Sort.by(Sort.Direction.DESC, "value") );

        return map;
    }

    @Override
    protected Map< List<String>, List<SearchCriteria> > getFilteringTestParams() {
        Map< List<String>, List<SearchCriteria> > map = new HashMap<>();

        map.put(List.of("courseId=1", "courseId!=2"),
                        List.of(
                            new SearchCriteria("courseId", EQUAL, 1L),
                            new SearchCriteria("courseId", NOT_EQUAL, 2L) ) );

        map.put(List.of("userId=1", "userId!=2"),
                        List.of(
                            new SearchCriteria("userId", EQUAL, 1L),
                            new SearchCriteria("userId", NOT_EQUAL, 2L) ) );

        map.put(List.of("value>1", "value>=2", "value<10", "value<=9", "value=7", "value!=8"),
                List.of(
                        new SearchCriteria("value", GREATER_THEN, 1),
                        new SearchCriteria("value", GREATER_OR_EQUAL, 2),
                        new SearchCriteria("value", LESS_THEN, 10),
                        new SearchCriteria("value", LESS_OR_EQUAL, 9),
                        new SearchCriteria("value", EQUAL, 7),
                        new SearchCriteria("value", NOT_EQUAL, 8)
                ) );

        return map;
    }

    @Override
    protected Map< String, String > getInvalidFilteringTestParams() {
        Map< String, String > map = new HashMap<>();

        map.put("fn=firstName1", "Operation 'EQUAL' is not supported for property firstName");

        return map;
    }

    @Override
    protected MarkSpecificationsBuilder getEntitySpecificationsBuilder() {
        return markSpecificationsBuilder;
    }

    @Override
    protected MarkService getService() {
        return markService;
    }
}
