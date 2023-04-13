package org.m.courses.api.v1.course;

import org.junit.jupiter.api.Test;
import org.m.courses.api.v1.common.AbstractControllerTest;
import org.m.courses.api.v1.controller.course.CourseRequest;
import org.m.courses.api.v1.controller.course.CourseResponse;
import org.m.courses.builder.CourseBuilder;
import org.m.courses.builder.UserBuilder;
import org.m.courses.filtering.CourseSpecificationsBuilder;
import org.m.courses.filtering.SearchCriteria;
import org.m.courses.filtering.UserSpecificationsBuilder;
import org.m.courses.model.Course;
import org.m.courses.model.User;
import org.m.courses.service.CourseService;
import org.m.courses.service.UserService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.m.courses.api.v1.controller.common.ApiPath.COURSE_API;
import static org.m.courses.filtering.FilteringOperation.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@WebMvcTest
public class CourseControllerTest extends AbstractControllerTest<Course, CourseRequest, CourseResponse> {

    @MockBean
    private CourseService courseService;

    @SpyBean
    private CourseSpecificationsBuilder courseSpecificationsBuilder;

    @MockBean
    private UserService userService;

    @MockBean
    private UserSpecificationsBuilder userEntitySpecificationsBuilder;

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
    protected Map<Consumer<CourseRequest>, Pair<String, String>> getCreateWithWrongValuesTestParameters() {
        Map<Consumer<CourseRequest>, Pair<String, String>> wrongValues = new HashMap<>();

        setupWrongValues( wrongValues );
        return wrongValues;
    }

    private void setupWrongValues(Map<Consumer<CourseRequest>, Pair<String, String>> wrongValues) {
        wrongValues.put(courseRequest -> courseRequest.setName(null), Pair.of("name", "must not be blank") );
        wrongValues.put(courseRequest -> courseRequest.setName(""), Pair.of("name", "must not be blank") );
        wrongValues.put(courseRequest -> courseRequest.setName("   "), Pair.of("name", "must not be blank") );
    }

    @Override
    protected Map<Consumer<CourseRequest>, Pair<String, String>> getUpdateWithWrongValuesTestParameters() {
        Map<Consumer<CourseRequest>, Pair<String, String>> wrongValues = new HashMap<>();

        setupWrongValues(wrongValues);

        return wrongValues;
    }

    @Override
    protected Map<Map<String, Object>, Pair<Function<Course, Object>, Object>> getPatchValuesTestParameters() {
        mockGetTeacher();
        Map<Map<String, Object>, Pair<Function<Course, Object>, Object>> map = new HashMap<>();

        User teacher = UserBuilder.builder().build();
        map.put(
                Map.of("teacher", teacher.getId()),
                Pair.of( Course::getTeacher, teacher ) );

        map.put(
                Map.of("name", "name1"),
                Pair.of( Course::getName, "name1" ) );

        map.put(
                Map.of("description", "description1"),
                Pair.of( Course::getDescription, "description1" ) );

        map.put(
                Map.of("lessonCount", 120),
                Pair.of( Course::getLessonCount, 120 ) );

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

    private void mockGetTeacher() {
        when( userService.get( anyLong() ) ).then( answer -> {
            User teacher = UserBuilder.builder().build();
            Long teacherId = answer.getArgument(0, Long.class);
            teacher.setId( teacherId );
            return teacher;
        } );
    }

    @Override
    protected Map< Consumer< CourseRequest >, Pair< Function<Course, Object>, Object> > getCreateWithOptionalValuesTestParameters() {
        Map<Consumer<CourseRequest>, Pair<Function<Course, Object>, Object>> optionalValues = new HashMap<>();

        return optionalValues;
    }

    @Override
    protected Map<Consumer<CourseRequest>, Pair<Function<Course, Object>, Object>> getUpdateWithOptionalValuesTestParameters() {
        return getCreateWithOptionalValuesTestParameters();
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

        map.put("fn=firstName1", "Operation 'EQUAL' is not supported for property firstName");

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
