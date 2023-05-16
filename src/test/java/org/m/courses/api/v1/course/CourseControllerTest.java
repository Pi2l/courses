package org.m.courses.api.v1.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.m.courses.api.v1.common.AbstractControllerTest;
import org.m.courses.api.v1.controller.course.CourseController;
import org.m.courses.api.v1.controller.course.CourseRequest;
import org.m.courses.api.v1.controller.course.CourseResponse;
import org.m.courses.builder.CourseBuilder;
import org.m.courses.builder.UserBuilder;
import org.m.courses.exception.AccessDeniedException;
import org.m.courses.filtering.CourseSpecificationsBuilder;
import org.m.courses.filtering.SearchCriteria;
import org.m.courses.model.Course;
import org.m.courses.model.User;
import org.m.courses.service.CourseService;
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

import static org.m.courses.api.v1.controller.common.ApiPath.COURSE_API;
import static org.m.courses.filtering.FilteringOperation.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
public class CourseControllerTest extends AbstractControllerTest<Course, CourseRequest, CourseResponse> {

    @MockBean
    private CourseService courseService;

    @SpyBean
    private CourseSpecificationsBuilder courseSpecificationsBuilder;

    @BeforeEach
    void init() {
        when(getService().get( anyLong() )).thenReturn( getNewEntity() );
    }

    @MockBean
    private UserService userService;

    @Override
    protected String getControllerPath() {
        return COURSE_API;
    }

    @Override
    protected Class<Course> getEntityClass() {
        return Course.class;
    }

    @Override
    protected Course getNewEntity() {
        return CourseBuilder.builder().setTeacher( UserBuilder.builder().build() ).build();
    }

    @Override
    protected CourseResponse convertToResponse(Course course) {
        return new CourseResponse( course );
    }

    @Override
    protected CourseRequest convertToRequest(Course course) {
        CourseRequest courseRequest = new CourseRequest();
        courseRequest.setTeacherId( course.getTeacher().getId() );
        courseRequest.setName( course.getName() );
        courseRequest.setDescription( course.getDescription() );
        courseRequest.setLessonCount( course.getLessonCount() );
        return courseRequest;
    }

    @Override
    protected List<Function<Course, Object>> getValueToBeUpdated() {
        List<Function<Course, Object>> valuesToBeUpdated = new ArrayList<>();

        valuesToBeUpdated.add(Course::getTeacher);
        valuesToBeUpdated.add(Course::getName);
        valuesToBeUpdated.add(Course::getDescription);
        valuesToBeUpdated.add(Course::getLessonCount);

        return valuesToBeUpdated;
    }

    @Override
    protected Map< Pair<Consumer<CourseRequest>, Runnable>, Pair<String, String>> getCreateWithWrongValuesTestParameters() {
        Map< Pair<Consumer<CourseRequest>, Runnable>, Pair<String, String>> wrongValues = new HashMap<>();

        wrongValues.put(
                Pair.of(request -> {
                            when(userService.get(anyLong())).thenReturn(null);
                            request.setTeacherId(834L);
                        }, this::mockGetTeacher ),
                Pair.of("cause", "teacher not found with id = 834") );

        wrongValues.put(
                Pair.of(request -> request.setLessonCount(0), () -> {}),
                Pair.of("lessonCount", "must be between 1 and 9223372036854775807") );

        setupWrongValues( wrongValues );

        return wrongValues;
    }

    private void setupWrongValues(Map< Pair<Consumer<CourseRequest>, Runnable>, Pair<String, String>> wrongValues) {
        wrongValues.put(Pair.of( req -> req.setName(null), () -> {}), Pair.of("name", "must not be blank") );
        wrongValues.put(Pair.of( req -> req.setName(""), () -> {}), Pair.of("name", "must not be blank") );
        wrongValues.put(Pair.of( req -> req.setName("   "), () -> {}), Pair.of("name", "must not be blank") );
    }

    @Override
    protected Map< Pair<Consumer<CourseRequest>, Runnable>, Pair<String, String>> getUpdateWithWrongValuesTestParameters() {

        return getCreateWithWrongValuesTestParameters();
    }

    @Override
    protected Map<Map<String, Object>, Pair<Function<Course, Object>, Supplier<Object> >> getPatchValuesTestParameters() {
        mockGetTeacher();
        Map<Map<String, Object>, Pair<Function<Course, Object>, Supplier<Object>>> map = new HashMap<>();

        User teacher = UserBuilder.builder().build();
        map.put(
                Map.of("teacherId", teacher.getId()),
                Pair.of( Course::getTeacher, () -> teacher ) );
        map.put(
                getNullValueMap("teacherId"),
                Pair.of( Course::getTeacher, () -> null ) );

        map.put(
                Map.of("name", "name1"),
                Pair.of( Course::getName, () -> "name1" ) );

        map.put(
                Map.of("description", "description1"),
                Pair.of( Course::getDescription, () -> "description1" ) );
        map.put(
                getNullValueMap("description"),
                Pair.of( Course::getDescription, () -> null ) );

        map.put(
                Map.of("lessonCount", 120),
                Pair.of( Course::getLessonCount, () -> 120 ) );
        map.put(
                getNullValueMap("lessonCount"),
                Pair.of( Course::getLessonCount, () -> null ) );

        return map;
    }

    private Map<String, Object> getNullValueMap(String field) {
        Map<String, Object> map = new HashMap<>();
        map.put(field, null);
        return map;
    }

    @Override
    protected Map<Map<String, Object>, Pair<Pair<String, Object>, Pair<Runnable, Runnable>>> getPatchInvalidValuesTestParameters() {
        Map<Map<String, Object>, Pair<Pair<String, Object>, Pair<Runnable, Runnable>>> map = new HashMap<>();

        getPatchInvalidValues(map);

        return map;
    }

    private void getPatchInvalidValues(Map<Map<String, Object>, Pair<Pair<String, Object>, Pair<Runnable, Runnable>>> map) {
        setupBlankField(map, "name");
        map.put(Map.of("teacherId", 834L),
                Pair.of(
                        Pair.of("cause", "teacher not found with id = 834"),
                        Pair.of( () -> {}, () -> {})
                ));
        map.put(Map.of("lessonCount", 0),
                Pair.of(
                        Pair.of("lessonCount", "must be between 1 and 9223372036854775807"),
                        Pair.of( () -> {}, () -> {})
                ));
    }

    private void setupBlankField(Map<Map<String, Object>, Pair<Pair<String, Object>, Pair<Runnable, Runnable>>> map, String fieldName) {
        map.put(
                Map.of(fieldName, ""),
                Pair.of(
                        Pair.of(fieldName, "must not be blank" ),
                        Pair.of(() -> {}, () -> {})
                ));
        map.put(
                Map.of(fieldName, "   "),
                Pair.of(
                        Pair.of(fieldName, "must not be blank" ),
                        Pair.of(() -> {}, () -> {})
                ));
        map.put(
                getNullValueMap(fieldName),
                Pair.of(
                        Pair.of(fieldName, "must not be blank" ),
                        Pair.of(() -> {}, () -> {})
                ));
    }

    @Test
    @Override
    public void createEntityTest() throws Exception {
        when( userService.get( anyLong() ) ).thenReturn( UserBuilder.builder().build() );
        super.createEntityTest();
    }

    @Test
    @Override
    public void updateEntity() throws Exception {
        mockGetTeacher();
        super.updateEntity();
    }

    @Override
    protected Map< Pair<Runnable, ResultMatcher>, Pair<String, Supplier<Object>> > getCreateServiceIllegalArgumentExceptionTest() {
        Map< Pair<Runnable, ResultMatcher>, Pair<String, Supplier<Object>> > map = new HashMap<>();

        map.put(
                Pair.of( () -> getCreateOrUpdateThrow(new AccessDeniedException()), status().isForbidden() ),
                Pair.of( "cause", () -> null ) );

        map.put(
                Pair.of( () -> getCreateOrUpdateThrow(new IllegalArgumentException("only teacher can lead the course or it can be null")), status().isBadRequest() ),
                Pair.of( "cause", () -> "only teacher can lead the course or it can be null" ) );

        map.put(
                Pair.of( () -> getCreateOrUpdateThrow(new IllegalArgumentException("entity cannot be null" )), status().isBadRequest() ),
                Pair.of( "cause", () -> "entity cannot be null" ) );

        return map;
    }

    @Override
    protected Map< Pair<Runnable, ResultMatcher>, Pair<String, Supplier<Object>> > getUpdateServiceIllegalArgumentExceptionTest() {
        return getCreateServiceIllegalArgumentExceptionTest();
    }

    private void getCreateOrUpdateThrow(Exception exception) {
        when( courseService.get( anyLong() ) ).thenReturn( getNewEntity() );
        mockGetTeacher();
        doThrow(exception)
                .when(courseService).update(any(Course.class));
        doThrow(exception)
                .when(courseService).create(any(Course.class));
    }
    // TODO: group + schedule

    private void mockGetTeacher() {
        when( userService.get( anyLong() ) ).then( answer -> {
            User teacher = UserBuilder.builder().build();
            Long teacherId = answer.getArgument(0, Long.class);
            teacher.setId( teacherId );
            return teacher;
        } );
    }

    @Override
    protected Map< List<String>, Sort > getSortingTestParams() {
        Map< List<String>, Sort > map = new HashMap<>();

        map.put( List.of("teacher"), Sort.by(Sort.Direction.ASC, "teacher") );
        map.put( List.of("teacher,desc"), Sort.by(Sort.Direction.DESC, "teacher") );

        map.put( List.of("name"), Sort.by(Sort.Direction.ASC, "name") );
        map.put( List.of("name,desc"), Sort.by(Sort.Direction.DESC, "name") );

        map.put( List.of("description"), Sort.by(Sort.Direction.ASC, "description") );
        map.put( List.of("description,desc"), Sort.by(Sort.Direction.DESC, "description") );

        map.put( List.of("lessonCount"), Sort.by(Sort.Direction.ASC, "lessonCount") );
        map.put( List.of("lessonCount,desc"), Sort.by(Sort.Direction.DESC, "lessonCount") );

        return map;
    }

    @Override
    protected Map< List<String>, List<SearchCriteria> > getFilteringTestParams() {
        Map< List<String>, List<SearchCriteria> > map = new HashMap<>();

        map.put(List.of("teacher=1", "teacher!=2", "teacher=null", "teacher!=null"),
                        List.of(
                            new SearchCriteria("teacher", EQUAL, 1L),
                            new SearchCriteria("teacher", NOT_EQUAL, 2L),
                            new SearchCriteria("teacher", EQUAL, null),
                            new SearchCriteria("teacher", NOT_EQUAL, null)
                        ) );

        map.put(List.of("name=name1", "name!=name2", "name:name3"),
                List.of(
                        new SearchCriteria("name", EQUAL, "name1"),
                        new SearchCriteria("name", NOT_EQUAL, "name2"),
                        new SearchCriteria("name", CONTAIN, "name3")
                ) );

        map.put(List.of("description=description1", "description!=description2", "description:description3"),
                List.of(
                        new SearchCriteria("description", EQUAL, "description1"),
                        new SearchCriteria("description", NOT_EQUAL, "description2"),
                        new SearchCriteria("description", CONTAIN, "description3")
                ) );

        map.put(List.of("lessonCount>1", "lessonCount>=2", "lessonCount<10", "lessonCount<=9", "lessonCount=7", "lessonCount!=8"),
                List.of(
                        new SearchCriteria("lessonCount", GREATER_THEN, 1),
                        new SearchCriteria("lessonCount", GREATER_OR_EQUAL, 2),
                        new SearchCriteria("lessonCount", LESS_THEN, 10),
                        new SearchCriteria("lessonCount", LESS_OR_EQUAL, 9),
                        new SearchCriteria("lessonCount", EQUAL, 7),
                        new SearchCriteria("lessonCount", NOT_EQUAL, 8)
                ) );
        return map;
    }

    @Override
    protected Map< String, String > getInvalidFilteringTestParams() {
        Map< String, String > map = new HashMap<>();

        map.put("fn=firstName1", "");

        return map;
    }

    @Override
    protected CourseSpecificationsBuilder getEntitySpecificationsBuilder() {
        return courseSpecificationsBuilder;
    }

    @Override
    protected CourseService getService() {
        return courseService;
    }
}
