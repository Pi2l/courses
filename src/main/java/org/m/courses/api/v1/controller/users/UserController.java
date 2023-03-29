package org.m.courses.api.v1.controller.users;

import org.m.courses.api.v1.controller.common.AbstractController;
import org.m.courses.model.User;
import org.m.courses.service.AbstractService;
import org.m.courses.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/users")
public class UserController extends AbstractController<User, UserRequest, UserResponse> {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
