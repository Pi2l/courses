package org.m.courses.api.v1.controller.group;

import org.m.courses.api.v1.controller.common.AbstractController;
import org.m.courses.api.v1.controller.common.UpdateValidationGroup;
import org.m.courses.exception.PatchFieldValidationException;
import org.m.courses.filtering.EntitySpecificationsBuilder;
import org.m.courses.filtering.GroupSpecificationsBuilder;
import org.m.courses.filtering.specification.EqualSpecification;
import org.m.courses.model.Group;
import org.m.courses.model.User;
import org.m.courses.service.AbstractService;
import org.m.courses.service.GroupService;
import org.m.courses.service.UserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Map;
import java.util.Set;

import static org.m.courses.api.v1.controller.common.ApiPath.GROUP_API;
import static org.m.courses.filtering.specification.SpecificationUtil.buildEqualSpec;


@RestController
@RequestMapping(GROUP_API)
public class GroupController extends AbstractController<Group, GroupRequest, GroupResponse> {

    private final GroupService groupService;
    private final UserService userService;
    private final Validator validator;
    private final ConversionService conversionService;
    private final GroupSpecificationsBuilder groupSpecificationsBuilder;

    public GroupController(GroupService groupService, UserService userService, ConversionService conversionService, Validator validator, GroupSpecificationsBuilder groupSpecificationsBuilder) {
        this.groupService = groupService;
        this.userService = userService;
        this.validator = validator;
        this.conversionService = conversionService;
        this.groupSpecificationsBuilder = groupSpecificationsBuilder;
    }

    @Override
    protected Group patchRequest(Map<String, Object> requestBody, Group group) {

        requestBody.entrySet().forEach( entry -> patchField(group, entry) );

        return group;
    }

    private void patchField(Group group, Map.Entry< String, Object > field) {
        switch (field.getKey()) {
            case "name":
                String name = conversionService.convert(field.getValue(), String.class);
                validateField("name", name);
                group.setName( name );
                return;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void validateField(String field, Object value) {
        Set< ConstraintViolation<GroupRequest> > validationViolations =
                validator.validateValue(GroupRequest.class, field, value, UpdateValidationGroup.class, Default.class);
        if ( validationViolations.isEmpty() ) {
            return;
        }
        throw new PatchFieldValidationException( validationViolations );
    }

    @Override
    protected GroupResponse convertToResponse(Group entity) {
        Page<User> users = userService.getAll(Pageable.unpaged(), buildEqualSpec("group_id", entity.getId()));
        if (users != null) {
            entity.setUsers( users.toSet() );
        }
        return new GroupResponse( entity );
    }

    @Override
    protected AbstractService<Group> getService() {
        return groupService;
    }

    @Override
    protected EntitySpecificationsBuilder<Group> getSpecificationBuilder() {
        return groupSpecificationsBuilder;
    }

    @Override
    protected ConversionService getConversionService() {
        return conversionService;
    }

}
