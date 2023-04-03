package org.m.courses.api.v1.controller.user;

import org.m.courses.api.v1.controller.common.AbstractController;
import org.m.courses.api.v1.controller.common.UpdateValidationGroup;
import org.m.courses.exception.PatchFieldValidationException;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.m.courses.service.AbstractService;
import org.m.courses.service.UserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/api/v1/users")
public class UserController extends AbstractController<User, UserRequest, UserResponse> {

    private final UserService userService;

    private final Validator validator;

    private ConversionService conversionService;

    public UserController(UserService userService, ConversionService conversionService) {
        this.userService = userService;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.conversionService = conversionService;
    }

    @Override
    protected User patchRequest(Map<String, Object> requestBody, User user) {

        requestBody.entrySet().forEach( entry -> patchField(user, entry) );

        return user;
    }

    private void patchField(User user, Map.Entry< String, Object > field) {
        String fieldValue;
        switch (field.getKey()) {
            case "firstName":
                fieldValue = conversionService.convert(field.getValue(), String.class);
                validateField("firstName", fieldValue);
                user.setFirstName( fieldValue );
                return;
            case "lastName":
                fieldValue = conversionService.convert(field.getValue(), String.class);
                validateField("lastName", fieldValue);
                user.setLastName( fieldValue );
                return;
            case "phoneNumber":
                fieldValue = conversionService.convert(field.getValue(), String.class);
                validateField("phoneNumber", fieldValue);
                user.setPhoneNumber( fieldValue );
                return;
            case "login":
                fieldValue = conversionService.convert(field.getValue(), String.class);
                validateField("login", fieldValue);
                user.setLogin( fieldValue );
                return;
            case "password":
                fieldValue = conversionService.convert(field.getValue(), String.class);
                validateField("password", fieldValue);
                user.setPassword( fieldValue );
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
}
