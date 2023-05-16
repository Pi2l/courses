package org.m.courses.api.v1.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.m.courses.api.v1.common.AbstractControllerTest;
import org.m.courses.api.v1.controller.schedule.ScheduleController;
import org.m.courses.api.v1.controller.schedule.ScheduleRequest;
import org.m.courses.api.v1.controller.schedule.ScheduleResponse;
import org.m.courses.builder.CourseBuilder;
import org.m.courses.builder.GroupBuilder;
import org.m.courses.builder.ScheduleBuilder;
import org.m.courses.exception.AccessDeniedException;
import org.m.courses.filtering.ScheduleSpecificationsBuilder;
import org.m.courses.filtering.SearchCriteria;
import org.m.courses.model.Course;
import org.m.courses.model.Group;
import org.m.courses.model.Schedule;
import org.m.courses.service.CourseService;
import org.m.courses.service.GroupService;
import org.m.courses.service.ScheduleService;
import org.m.courses.service.UserService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.m.courses.api.v1.controller.common.ApiPath.SCHEDULE_API;
import static org.m.courses.filtering.FilteringOperation.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest( ScheduleController.class )
public class ScheduleControllerTest extends AbstractControllerTest<Schedule, ScheduleRequest, ScheduleResponse> {

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private GroupService groupService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private UserService userService;

    @SpyBean
    private ScheduleSpecificationsBuilder scheduleSpecificationsBuilder;

    @BeforeEach
    void init() {
        mockGetCourse();
        mockGetGroup();
        when(getService().get( anyLong() )).thenReturn( getNewEntity() );
    }

    private void mockGetGroup() {
        when( groupService.get( anyLong() ) )
                .thenAnswer( answer -> GroupBuilder.builder().setId( answer.getArgument(0) ).build() );
    }

    private void mockGetCourse() {
        when( courseService.get( anyLong() ) )
                .thenAnswer( answer -> CourseBuilder.builder().setId( answer.getArgument(0) ).build() );

        mockGetAllCourses( Set.of() );
    }

    private void mockGetAllCourses(Set<Course> courses) {

        Page<Course> page = mock(Page.class);
        when( page.toSet()).thenReturn( courses );

        when( courseService.getAll( any(Pageable.class), any() ) ).thenReturn(page);
    }

    @Override
    protected String getControllerPath() {
        return SCHEDULE_API;
    }

    @Override
    protected Class<Schedule> getEntityClass() {
        return Schedule.class;
    }

    @Override
    protected Schedule getNewEntity() {
        return ScheduleBuilder.builder()
                .setCourse( CourseBuilder.builder().build() )
                .setGroup( GroupBuilder.builder().build() )
                .build();
    }

    @Override
    protected ScheduleResponse convertToResponse(Schedule entity) {
        return new ScheduleResponse( entity );
    }

    @Override
    protected ScheduleRequest convertToRequest(Schedule entity) {
        ScheduleRequest request = new ScheduleRequest();
        request.setCourseId( entity.getCourse().getId() );
        request.setGroupId( entity.getGroup().getId() );
        request.setStartAt( entity.getStartAt() );
        request.setEndAt( entity.getEndAt() );
        return request;
    }

    @Override
    protected List<Function<Schedule, Object>> getValueToBeUpdated() {
        List< Function<Schedule, Object> > valuesToBeUpdated = new ArrayList<>();

        valuesToBeUpdated.add(Schedule::getCourse);
        valuesToBeUpdated.add(Schedule::getGroup);
        valuesToBeUpdated.add(Schedule::getStartAt);
        valuesToBeUpdated.add(Schedule::getEndAt);
        return valuesToBeUpdated;
    }

    @Override
    protected Map<Pair<Consumer<ScheduleRequest>, Runnable>, Pair<String, String>> getCreateWithWrongValuesTestParameters() {
        Map<Pair<Consumer<ScheduleRequest>, Runnable>, Pair<String, String>> wrongValues = new HashMap<>();

        setupWrongValues(wrongValues);

        return wrongValues;
    }

    private void setupWrongValues(Map< Pair<Consumer<ScheduleRequest>, Runnable>, Pair<String, String>> wrongValues) {
        wrongValues.put( Pair.of( req -> req.setCourseId(null), () -> {} ), Pair.of("courseId", "must not be null") );
        wrongValues.put( Pair.of( req -> req.setGroupId(null), () -> {} ), Pair.of("groupId", "must not be null") );
        wrongValues.put( Pair.of( req -> req.setStartAt(null), () -> {} ), Pair.of("startAt", "must not be null") );
        wrongValues.put( Pair.of( req -> req.setEndAt(null), () -> {} ), Pair.of("endAt", "must not be null") );

        Course course = CourseBuilder.builder().build();
        wrongValues.put(
                Pair.of( request -> {
                            when( courseService.get( anyLong() ) ).thenReturn( null );
                            request.setCourseId(course.getId());
                        }, this::mockGetCourse ),
                Pair.of( "cause", "course not found with id = " + course.getId() ) );

        Group group = GroupBuilder.builder().build();
        wrongValues.put(
                Pair.of( request -> {
                            when( groupService.get( anyLong() ) ).thenReturn( null );
                            request.setGroupId(group.getId());
                        }, this::mockGetGroup ),
                Pair.of( "cause", "group not found with id = " + group.getId() ) );
    }

    @Override
    protected Map< Pair<Consumer<ScheduleRequest>, Runnable>, Pair<String, String>> getUpdateWithWrongValuesTestParameters() {
        Map< Pair<Consumer<ScheduleRequest>, Runnable>, Pair<String, String>> wrongValues = new HashMap<>();

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

        map.put(
                Pair.of( () -> getCreateOrUpdateThrow(new IllegalArgumentException("endAt must be after now")), status().isBadRequest() ),
                Pair.of( "cause", () -> "endAt must be after now" ) );
        map.put(
                Pair.of( () -> getCreateOrUpdateThrow(new IllegalArgumentException("startAt must be before endAt")), status().isBadRequest() ),
                Pair.of( "cause", () -> "startAt must be before endAt" ) );
        map.put(
                Pair.of( () -> getCreateOrUpdateThrow(new IllegalArgumentException("group has not such course with courseId = 1")), status().isBadRequest() ),
                Pair.of( "cause", () -> "group has not such course with courseId = 1") );
        return map;
    }

    private void getCreateOrUpdateThrow(Exception exception) {
        when(scheduleService.get( anyLong() )).thenReturn( getNewEntity() );
        doThrow(exception)
                .when(scheduleService).update( any(Schedule.class) );
        doThrow(exception)
                .when(scheduleService).create( any(Schedule.class) );
    }

    @Override
    protected Map<Pair<Runnable, ResultMatcher>, Pair<String, Supplier<Object>>> getUpdateServiceIllegalArgumentExceptionTest() {
        return getCreateServiceIllegalArgumentExceptionTest();
    }

    @Override
    protected Map<Map<String, Object>, Pair<Function<Schedule, Object>, Supplier<Object>>> getPatchValuesTestParameters() {
        Map<Map<String, Object>, Pair<Function<Schedule, Object>, Supplier<Object>>> map = new HashMap<>();

        Course course = CourseBuilder.builder().build();
        map.put(
                Map.of("courseId", course.getId()),
                Pair.of( Schedule::getCourse, () -> course ) );

        Group group = GroupBuilder.builder().build();
        map.put(
                Map.of("groupId", group.getId()),
                Pair.of( Schedule::getGroup, () -> group ) );

        String dateString = "2023-04-28T13:15:25.25+03:00[Europe/Kyiv]";
        ZonedDateTime dateTime = ZonedDateTime.parse(dateString);
        map.put(
                Map.of("startAt", dateString),
                Pair.of( Schedule::getStartAt, () -> dateTime ) );
        map.put(
                Map.of("endAt", dateString),
                Pair.of( Schedule::getEndAt, () -> dateTime ) );

        return map;
    }

    @Override
    protected Map<Map<String, Object>, Pair<Pair<String, Object>, Pair<Runnable, Runnable>>> getPatchInvalidValuesTestParameters() {
        Map<Map<String, Object>, Pair<Pair<String, Object>, Pair<Runnable, Runnable>>> map = new HashMap<>();

        getPatchInvalidValues(map);

        Course course = CourseBuilder.builder().build();
        map.put(
                Map.of("courseId", course.getId()),
                Pair.of(
                        Pair.of("cause", "course not found with id = " + course.getId() ),
                        Pair.of(
                                this::getCreateOrUpdateReturnNull, () -> { mockGetCourse(); mockGetGroup(); } )
                ));

        Group group = GroupBuilder.builder().build();
        map.put(
                Map.of("groupId", group.getId()),
                Pair.of(
                        Pair.of("cause", "group not found with id = " + group.getId() ),
                        Pair.of(
                                this::getCreateOrUpdateReturnNull, () -> { mockGetCourse(); mockGetGroup(); } )
                ));
        return map;
    }

    private void getCreateOrUpdateReturnNull() {
        when( courseService.get( anyLong() ) ).thenReturn( null );
        when( groupService.get( anyLong() ) ).thenReturn( null );
    }

    private void getPatchInvalidValues(Map<Map<String, Object>, Pair<Pair<String, Object>, Pair<Runnable, Runnable>>> map) {
        setupNullField(map, "startAt");
        setupNullField(map, "endAt");
        setupNullField(map, "groupId");
        setupNullField(map, "courseId");
    }

    private void setupNullField(Map<Map<String, Object>, Pair<Pair<String, Object>, Pair<Runnable, Runnable>>> map, String fieldName) {
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put(fieldName, null);
        map.put(
                fieldMap,
                Pair.of(
                        Pair.of(fieldName, "must not be null"),
                        Pair.of( () -> {}, () -> {} )
                ));
    }

    @Override
    protected Map< Consumer< ScheduleRequest >, Pair< Function<Schedule, Object>, Object> > getCreateWithOptionalValuesTestParameters() {
        Map<Consumer<ScheduleRequest>, Pair<Function<Schedule, Object>, Object>> optionalValues = new HashMap<>();

        return optionalValues;
    }

    @Override
    protected Map<Consumer<ScheduleRequest>, Pair<Function<Schedule, Object>, Object>> getUpdateWithOptionalValuesTestParameters() {
        return getCreateWithOptionalValuesTestParameters();
    }

    @Override
    protected Map< List<String>, Sort > getSortingTestParams() {
        Map< List<String>, Sort > map = new HashMap<>();

        map.put( List.of("groupId"), Sort.by(Sort.Direction.ASC, "groupId") );
        map.put( List.of("groupId,desc"), Sort.by(Sort.Direction.DESC, "groupId") );

        map.put( List.of("courseId"), Sort.by(Sort.Direction.ASC, "courseId") );
        map.put( List.of("courseId,desc"), Sort.by(Sort.Direction.DESC, "courseId") );

        map.put( List.of("startAt"), Sort.by(Sort.Direction.ASC, "startAt") );
        map.put( List.of("startAt,desc"), Sort.by(Sort.Direction.DESC, "startAt") );

        map.put( List.of("endAt"), Sort.by(Sort.Direction.ASC, "endAt") );
        map.put( List.of("endAt,desc"), Sort.by(Sort.Direction.DESC, "endAt") );

        return map;
    }

    @Override
    protected Map< List<String>, List<SearchCriteria> > getFilteringTestParams() {
        Map< List<String>, List<SearchCriteria> > map = new HashMap<>();
        ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

        map.put(List.of("courseId=1", "courseId!=2"),
                        List.of(
                            new SearchCriteria("courseId", EQUAL, 1L),
                            new SearchCriteria("courseId", NOT_EQUAL, 2L) ) );

        map.put(List.of("groupId=1", "groupId!=2"),
                        List.of(
                            new SearchCriteria("groupId", EQUAL, 1L),
                            new SearchCriteria("groupId", NOT_EQUAL, 2L) ) );

        map.put(List.of("startAt=2023-04-28T13:15:25.25+03:00[Europe/Kyiv]", "startAt!=2023-05-28T13:15:25.25+03:00[Europe/Kyiv]",
                        "startAt>2023-04-29T13:15:25.25+03:00[Europe/Kyiv]", "startAt>=2023-04-28T13:15:25.25+03:00[Europe/Kyiv]",
                        "startAt<2023-05-29T13:15:25.25+03:00[Europe/Kyiv]", "startAt<=2023-05-28T13:15:25.25+03:00[Europe/Kyiv]" ),
                        List.of(
                            new SearchCriteria("startAt", EQUAL, getZonedDateTime("2023-04-28T13:15:25.25", DEFAULT_ZONE_ID)),
                            new SearchCriteria("startAt", NOT_EQUAL, getZonedDateTime("2023-05-28T13:15:25.25", DEFAULT_ZONE_ID)),
                            new SearchCriteria("startAt", GREATER_THEN, getZonedDateTime("2023-04-29T13:15:25.25", DEFAULT_ZONE_ID)),
                            new SearchCriteria("startAt", GREATER_OR_EQUAL, getZonedDateTime("2023-04-28T13:15:25.25", DEFAULT_ZONE_ID)),
                            new SearchCriteria("startAt", LESS_THEN, getZonedDateTime("2023-05-29T13:15:25.25", DEFAULT_ZONE_ID)),
                            new SearchCriteria("startAt", LESS_OR_EQUAL, getZonedDateTime("2023-05-28T13:15:25.25", DEFAULT_ZONE_ID))
                        ) );

        map.put(List.of("endAt=2023-04-28T13:15:25.25Z", "endAt!=2023-05-28T13:15:25.25+03:00[Europe/Kyiv]",
                        "endAt>2023-04-29T13:15:25.25+03:00[Europe/Kyiv]", "endAt>=2023-04-28T13:15:25.25+03:00[Europe/Kyiv]",
                        "endAt<2023-05-29T13:15:25.25+03:00[Europe/Kyiv]", "endAt<=2023-05-28T13:15:25.25+03:00[Europe/Kyiv]" ),
                List.of(
                        new SearchCriteria("endAt", EQUAL, getZonedDateTime("2023-04-28T13:15:25.25", ZoneId.of("Z"))),
                        new SearchCriteria("endAt", NOT_EQUAL, getZonedDateTime("2023-05-28T13:15:25.25", DEFAULT_ZONE_ID)),
                        new SearchCriteria("endAt", GREATER_THEN, getZonedDateTime("2023-04-29T13:15:25.25", DEFAULT_ZONE_ID)),
                        new SearchCriteria("endAt", GREATER_OR_EQUAL, getZonedDateTime("2023-04-28T13:15:25.25", DEFAULT_ZONE_ID)),
                        new SearchCriteria("endAt", LESS_THEN, getZonedDateTime("2023-05-29T13:15:25.25", DEFAULT_ZONE_ID)),
                        new SearchCriteria("endAt", LESS_OR_EQUAL, getZonedDateTime("2023-05-28T13:15:25.25", DEFAULT_ZONE_ID))
                ) );

        return map;
    }

    private ZonedDateTime getZonedDateTime(String date, ZoneId zoneId) {
        return ZonedDateTime.of(LocalDateTime.parse(date), zoneId);
    }

    @Override
    protected Map< String, String > getInvalidFilteringTestParams() {
        Map< String, String > map = new HashMap<>();

        map.put("fn=firstName1", "Operation 'EQUAL' is not supported for property firstName");

        return map;
    }

    @Override
    protected ScheduleSpecificationsBuilder getEntitySpecificationsBuilder() {
        return scheduleSpecificationsBuilder;
    }

    @Override
    protected ScheduleService getService() {
        return scheduleService;
    }
}
