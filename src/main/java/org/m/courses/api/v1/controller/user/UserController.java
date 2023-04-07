package org.m.courses.api.v1.controller.user;

import org.m.courses.api.v1.controller.common.AbstractController;
import org.m.courses.api.v1.controller.common.UpdateValidationGroup;
import org.m.courses.exception.PatchFieldValidationException;
import org.m.courses.exception.UniqueFieldViolationException;
import org.m.courses.filtering.EntitySpecificationsBuilder;
import org.m.courses.filtering.UserSpecificationsBuilder;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.m.courses.service.AbstractService;
import org.m.courses.service.UserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/api/v1/users")
public class UserController extends AbstractController<User, UserRequest, UserResponse> {

    private final UserService userService;

    private final Validator validator;

    private final ConversionService conversionService;

    private final UserSpecificationsBuilder userSpecificationsBuilder;

    public UserController(UserService userService, ConversionService conversionService, Validator validator, UserSpecificationsBuilder userSpecificationsBuilder) {
        this.userService = userService;
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
                String firstName = conversionService.convert(field.getValue(), String.class);
                validateField("firstName", firstName);
                user.setFirstName( firstName );
                return;
            case "lastName":
                String lastName = conversionService.convert(field.getValue(), String.class);
                validateField("lastName", lastName);
                user.setLastName( lastName );
                return;
            case "phoneNumber":
                String phoneNumber = conversionService.convert(field.getValue(), String.class);
                validateField("phoneNumber", phoneNumber);
                user.setPhoneNumber( phoneNumber );
                return;
            case "login":
                String login = conversionService.convert(field.getValue(), String.class);
                validateField("login", login);
                user.setLogin( login );
                return;
            case "password":
                String password = conversionService.convert(field.getValue(), String.class);
                validateField("password", password);
                user.setPassword( password );
                return;
            case "role":
                Object roleObject = field.getValue();
                Role role;
                if (roleObject != null) {
                    role = Role.valueOf(String.valueOf( roleObject ));
                } else {
                    role = null;
                }
                validateField("role", role);
                user.setRole( role );
                return;
            default:
                throw new IllegalArgumentException();
        }
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
    protected User createEntity(User entity) {
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
    protected User updateEntity(User entity) {
        if (userService.isUnique( entity )) {
            return getService().update( entity );
        }
        throw new UniqueFieldViolationException("login");
    }

    @Override
    protected User patchEntity(User entity) {
        return updateEntity(entity);
    }
}
