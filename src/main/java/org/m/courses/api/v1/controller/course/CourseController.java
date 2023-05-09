package org.m.courses.api.v1.controller.course;

import org.m.courses.api.v1.controller.common.AbstractController;
import org.m.courses.api.v1.controller.common.UpdateValidationGroup;
import org.m.courses.exception.ItemNotFoundException;
import org.m.courses.exception.PatchFieldValidationException;
import org.m.courses.filtering.CourseSpecificationsBuilder;
import org.m.courses.filtering.EntitySpecificationsBuilder;
import org.m.courses.model.Course;
import org.m.courses.model.User;
import org.m.courses.service.CourseService;
import org.m.courses.service.UserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Map;
import java.util.Set;

import static org.m.courses.api.v1.controller.common.ApiPath.COURSE_API;


@RestController
@RequestMapping(COURSE_API)
public class CourseController extends AbstractController<Course, CourseRequest, CourseResponse> {

    private final CourseService courseService;
    private final UserService userService;
    private final Validator validator;
    private final ConversionService conversionService;
    private final CourseSpecificationsBuilder courseSpecificationsBuilder;

    public CourseController(CourseService courseService, UserService userService, ConversionService conversionService, Validator validator, CourseSpecificationsBuilder courseSpecificationsBuilder) {
        this.courseService = courseService;
        this.userService = userService;
        this.validator = validator;
        this.conversionService = conversionService;
        this.courseSpecificationsBuilder = courseSpecificationsBuilder;
    }

    @Override
    protected Course patchRequest(Map<String, Object> requestBody, Course user) {

        requestBody.entrySet().forEach( entry -> patchField(user, entry) );
        return user;
    }

    @Override
    public Course createEntity(Course course, CourseRequest request) {
        User user = getTeacher( request.getTeacherId() );

        course.setTeacher( user );

        return super.createEntity( course, request );
    }

    private User getTeacher(Long teacherId) {
        if (teacherId != null) {
            User user = userService.get( teacherId );
            if (user == null) {
                throw new ItemNotFoundException("teacher not found with id = " + teacherId );
            }
            return user;
        }
        return null;
    }

    @Override
    public Course updateEntity(Course course, CourseRequest request) {
        User user = getTeacher( request.getTeacherId() );

        course.setTeacher( user );

        return super.updateEntity( course, request );
    }

    private void patchField(Course course, Map.Entry< String, Object > field) {
        switch ( field.getKey() ) {
            case "teacherId":
                Long teacherId = conversionService.convert(field.getValue(), Long.class);
                validateField("teacherId", teacherId);

                User teacher = getTeacher(teacherId);
                course.setTeacher( teacher );
                return;
            case "name":
                String name = conversionService.convert(field.getValue(), String.class);
                validateField("name", name);
                course.setName( name );
                return;
            case "description":
                String description = conversionService.convert(field.getValue(), String.class);
                validateField("description", description);
                course.setDescription( description );
                return;
            case "lessonCount":
                Integer lessonCount = conversionService.convert(field.getValue(), Integer.class);
                validateField("lessonCount", lessonCount);
                course.setLessonCount( lessonCount );
                return;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void validateField(String field, Object value) {
        Set< ConstraintViolation<CourseRequest> > validationViolations =
                validator.validateValue(CourseRequest.class, field, value, UpdateValidationGroup.class, Default.class);
        if ( validationViolations.isEmpty() ) {
            return;
        }
        throw new PatchFieldValidationException( validationViolations );
    }

    @Override
    protected CourseResponse convertToResponse(Course user) {
        return new CourseResponse( user );
    }

    @Override
    protected CourseService getService() {
        return courseService;
    }

    @Override
    protected EntitySpecificationsBuilder<Course> getSpecificationBuilder() {
        return courseSpecificationsBuilder;
    }

    @Override
    protected ConversionService getConversionService() {
        return conversionService;
    }

}
