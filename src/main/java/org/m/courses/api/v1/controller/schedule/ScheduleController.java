package org.m.courses.api.v1.controller.schedule;

import org.m.courses.api.v1.controller.common.AbstractController;
import org.m.courses.api.v1.controller.common.UpdateValidationGroup;
import org.m.courses.exception.ItemNotFoundException;
import org.m.courses.exception.PatchFieldValidationException;
import org.m.courses.filtering.EntitySpecificationsBuilder;
import org.m.courses.filtering.ScheduleSpecificationsBuilder;
import org.m.courses.model.Course;
import org.m.courses.model.Group;
import org.m.courses.model.Schedule;
import org.m.courses.service.AbstractService;
import org.m.courses.service.CourseService;
import org.m.courses.service.GroupService;
import org.m.courses.service.ScheduleService;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

import static org.m.courses.api.v1.controller.common.ApiPath.SCHEDULE_API;
import static org.m.courses.filtering.specification.SpecificationUtil.buildEqualSpec;


@RestController
@RequestMapping(SCHEDULE_API)
public class ScheduleController extends AbstractController<Schedule, ScheduleRequest, ScheduleResponse> {

    private final ScheduleService scheduleService;
    private final CourseService courseService;
    private final GroupService groupService;
    private final Validator validator;
    private final ConversionService conversionService;
    private final ScheduleSpecificationsBuilder scheduleSpecificationsBuilder;

    public ScheduleController(ScheduleService scheduleService, CourseService courseService, GroupService groupService, ConversionService conversionService, Validator validator, ScheduleSpecificationsBuilder scheduleSpecificationsBuilder) {
        this.scheduleService = scheduleService;
        this.courseService = courseService;
        this.groupService = groupService;
        this.validator = validator;
        this.conversionService = conversionService;
        this.scheduleSpecificationsBuilder = scheduleSpecificationsBuilder;
    }

    @Override
    protected Schedule patchRequest(Map<String, Object> requestBody, Schedule entity) {

        requestBody.entrySet().forEach( entry -> patchField(entity, entry) );

        return entity;
    }

    private void patchField(Schedule entity, Map.Entry< String, Object > field) {
        switch (field.getKey()) {
            case "courseId":
                Long courseId = conversionService.convert(field.getValue(), Long.class);
                validateField("courseId", courseId);

                Course course = getCourse(courseId);
                entity.setCourse( course );
                return;

            case "startAt":
                ZonedDateTime startAtLocal = conversionService.convert(field.getValue(), ZonedDateTime.class);
                validateField("startAt", startAtLocal);

                entity.setStartAt( startAtLocal );
                return;

            case "endAt":
                ZonedDateTime endAtLocal = conversionService.convert(field.getValue(), ZonedDateTime.class);
                validateField("endAt", endAtLocal);

                entity.setEndAt( endAtLocal );
                return;

            case "groupId":
                Long groupId = conversionService.convert(field.getValue(), Long.class);
                validateField("groupId", groupId);

                Group group = getGroup(groupId);
                entity.setGroup( group );
                return;
            default:
                throw new IllegalArgumentException();
        }
    }

    private Group getGroup(Long groupId) {
        Group group = groupService.get( groupId );
        if (group == null) {
            throw new ItemNotFoundException("group not found with id = " + groupId );
        }
        group.setCourses( courseService.getAll(Pageable.unpaged(), buildEqualSpec("group", groupId) ).toSet() );
        return group;
    }

    private Course getCourse(Long courseId) {
        Course course = courseService.get( courseId );
        if (course == null) {
            throw new ItemNotFoundException("course not found with id = " + courseId );
        }
        return course;
    }

    @Override
    protected Schedule createEntity(Schedule entity, ScheduleRequest request) {
        Course course = getCourse( request.getCourseId() );
        Group group = getGroup( request.getGroupId() );

        entity.setCourse( course );
        entity.setGroup( group );
        return super.createEntity( entity, request );
    }

    @Override
    protected Schedule updateEntity(Schedule entity, ScheduleRequest request) {
        Course course = getCourse( request.getCourseId() );
        Group group = getGroup( request.getGroupId() );

        entity.setCourse( course );
        entity.setGroup( group );
        return super.updateEntity( entity, request );
    }

    private void validateField(String field, Object value) {
        Set< ConstraintViolation<ScheduleRequest> > validationViolations =
                validator.validateValue(ScheduleRequest.class, field, value, UpdateValidationGroup.class, Default.class);
        if ( validationViolations.isEmpty() ) {
            return;
        }
        throw new PatchFieldValidationException( validationViolations );
    }

    @Override
    protected ScheduleResponse convertToResponse(Schedule entity) {
        return new ScheduleResponse( entity );
    }

    @Override
    protected AbstractService<Schedule> getService() {
        return scheduleService;
    }

    @Override
    protected EntitySpecificationsBuilder<Schedule> getSpecificationBuilder() {
        return scheduleSpecificationsBuilder;
    }

    @Override
    protected ConversionService getConversionService() {
        return conversionService;
    }

}
