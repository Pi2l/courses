package org.m.courses.api.v1.controller.user;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.m.courses.api.v1.controller.common.AbstractController;
import org.m.courses.api.v1.controller.common.UpdateValidationGroup;
import org.m.courses.exception.ItemNotFoundException;
import org.m.courses.exception.PatchFieldValidationException;
import org.m.courses.exception.UniqueFieldViolationException;
import org.m.courses.filtering.EntitySpecificationsBuilder;
import org.m.courses.filtering.UserSpecificationsBuilder;
import org.m.courses.model.Group;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.m.courses.service.AbstractService;
import org.m.courses.service.GroupService;
import org.m.courses.service.UserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import static org.m.courses.api.v1.controller.common.ApiPath.USER_API;


@RestController
@RequestMapping(USER_API)
@Tag(name = "Users", description = "The Users API")
public class UserController extends AbstractController<User, UserRequest, UserResponse> {

    private final UserService userService;
    private final GroupService groupService;
    private final Validator validator;
    private final ConversionService conversionService;
    private final UserSpecificationsBuilder userSpecificationsBuilder;

    public UserController(UserService userService, GroupService groupService, ConversionService conversionService, Validator validator, UserSpecificationsBuilder userSpecificationsBuilder) {
        this.userService = userService;
        this.groupService = groupService;
        this.validator = validator;
        this.conversionService = conversionService;
        this.userSpecificationsBuilder = userSpecificationsBuilder;
    }

    @Override
    protected User patchRequest(Map<String, Object> requestBody, User user) {

        requestBody.entrySet().forEach( entry -> patchField(user, entry) );

        return user;
    }

    private void patchField(User user, Map.Entry< String, Object > field) {
        switch (field.getKey()) {
            case "firstName":
                writeValidValue( field, String.class, user, User :: setFirstName);
                return;
            case "lastName":
                writeValidValue( field, String.class, user, User :: setLastName);
                return;
            case "phoneNumber":
                writeValidValue( field, String.class, user, User :: setPhoneNumber);
                return;
            case "login":
                writeValidValue( field, String.class, user, User :: setLogin);
                return;
            case "password":
                writeValidValue( field, String.class, user, User :: setPassword);
                return;
            case "role":
                Object roleObject = field.getValue();
                Role role = null;
                if (roleObject != null) {
                    role = Role.valueOf(String.valueOf( roleObject ));
                }
                validateField("role", role);
                user.setRole( role );
                return;
            case "groupId":
                Long groupId = getValidValue( field, Long.class );
                Group group = getGroup(groupId);
                user.setGroup( group );
                return;
            default:
                throw new IllegalArgumentException();
        }
    }

    private <T> void writeValidValue(Map.Entry< String, Object > field, Class<T> clazz, User user, BiConsumer<User, T> setter){
        T value = getValidValue(field,clazz );
        setter.accept(user,value);
    }
    private <T> T getValidValue( Map.Entry< String, Object > field, Class<T> clazz ){
        T value = conversionService.convert(field.getValue(), clazz );
        validateField(field.getKey(), value);
        return value;
    }

    private void validateField(String field, Object value) {
        Set< ConstraintViolation< UserRequest > > validationViolations =
                validator.validateValue(UserRequest.class, field, value, UpdateValidationGroup.class, Default.class);
        if ( validationViolations.isEmpty() ) {
            return;
        }
        throw new PatchFieldValidationException( validationViolations );
    }

    @Override
    protected UserResponse convertToResponse(User user) {
        return new UserResponse( user );
    }

    @Override
    protected AbstractService<User> getService() {
        return userService;
    }

    @Override
    protected EntitySpecificationsBuilder<User> getSpecificationBuilder() {
        return userSpecificationsBuilder;
    }

    @Override
    protected User createEntity(User entity, UserRequest request) {
        setGroup(entity, request);

        if (userService.isUnique( entity )) {
            return getService().create(entity);
        }
        throw new UniqueFieldViolationException("login");
    }

    @Override
    protected ConversionService getConversionService() {
        return conversionService;
    }

    @Override
    protected User updateEntity(User entity, UserRequest request) {
        setGroup(entity, request);

        return updateUser(entity);
    }

    private User updateUser(User entity) {
        if (userService.isUnique(entity)) {
            return getService().update(entity);
        }
        throw new UniqueFieldViolationException("login");
    }

    private void setGroup(User entity, UserRequest request) {
        Group group = getGroup( request.getGroupId() );
        entity.setGroup( group );
    }

    private Group getGroup(Long groupId) {
        if (groupId != null) {
            Group group = groupService.get( groupId );
            if (group == null) {
                throw new ItemNotFoundException("group not found with id = " + groupId );
            }
            return group;
        }
        return null;
    }

    @Override
    protected User patchEntity(User entity, Map<String, Object> request) {
        return updateUser(entity);
    }
}
