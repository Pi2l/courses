package org.m.courses.api.v1.controller.mark;

import org.m.courses.api.v1.controller.common.AbstractController;
import org.m.courses.api.v1.controller.common.UpdateValidationGroup;
import org.m.courses.exception.ItemNotFoundException;
import org.m.courses.exception.PatchFieldValidationException;
import org.m.courses.filtering.EntitySpecificationsBuilder;
import org.m.courses.filtering.MarkSpecificationsBuilder;
import org.m.courses.model.Course;
import org.m.courses.model.Group;
import org.m.courses.model.Mark;
import org.m.courses.model.User;
import org.m.courses.service.AbstractService;
import org.m.courses.service.CourseService;
import org.m.courses.service.GroupService;
import org.m.courses.service.MarkService;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Map;
import java.util.Set;

import static org.m.courses.api.v1.controller.common.ApiPath.MARK_API;


@RestController
@RequestMapping(MARK_API)
public class MarkController extends AbstractController<Mark, MarkRequest, MarkResponse> {

    private final MarkService markService;
    private final CourseService courseService;
    private final GroupService groupService;
    private final Validator validator;
    private final ConversionService conversionService;
    private final MarkSpecificationsBuilder markSpecificationsBuilder;

    public MarkController(MarkService markService, CourseService courseService, GroupService groupService,
                          ConversionService conversionService, Validator validator, MarkSpecificationsBuilder markSpecificationsBuilder) {
        this.markService = markService;
        this.courseService = courseService;
        this.groupService = groupService;
        this.validator = validator;
        this.conversionService = conversionService;
        this.markSpecificationsBuilder = markSpecificationsBuilder;
    }

    @Override
    protected Mark patchRequest(Map<String, Object> requestBody, Mark entity) {

        requestBody.entrySet().forEach( entry -> patchField(entity, entry) );

        return entity;
    }

    private void patchField(Mark entity, Map.Entry< String, Object > field) {
        switch (field.getKey()) {
            case "courseId":
                Long courseId = conversionService.convert(field.getValue(), Long.class);
                validateField("courseId", courseId);

//                Course course = getCourse(courseId);
//                entity.setCourse( course );
                return;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    protected Mark createEntity(Mark entity, MarkRequest request) {
//        Course course = getCourse( request.getCourseId() );
//        Group group = getGroup( request.getGroupId() );
//
//        entity.setCourse( course );
//        entity.setGroup( group );
        return super.createEntity( entity, request );
    }

    @Override
    protected Mark updateEntity(Mark entity, MarkRequest request) {
//        Course course = getCourse( request.getCourseId() );
//        Group group = getGroup( request.getGroupId() );
//
//        entity.setCourse( course );
//        entity.setGroup( group );
        return super.updateEntity( entity, request );
    }

    private User getTeacher(Long teacherId) {
//        if (teacherId != null) {
//            User user = userService.get( teacherId );
//            if (user == null) {
//                throw new ItemNotFoundException("teacher not found with id = " + teacherId );
//            }
//            return user;
//        }
        return null;
    }

    private void validateField(String field, Object value) {
        Set< ConstraintViolation<MarkRequest> > validationViolations =
                validator.validateValue(MarkRequest.class, field, value, UpdateValidationGroup.class, Default.class);
        if ( validationViolations.isEmpty() ) {
            return;
        }
        throw new PatchFieldValidationException( validationViolations );
    }

    @Override
    protected MarkResponse convertToResponse(Mark entity) {
        return new MarkResponse( entity );
    }

    @Override
    protected AbstractService<Mark> getService() {
        return markService;
    }

    @Override
    protected EntitySpecificationsBuilder<Mark> getSpecificationBuilder() {
        return markSpecificationsBuilder;
    }

    @Override
    protected ConversionService getConversionService() {
        return conversionService;
    }

}
