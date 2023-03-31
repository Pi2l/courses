package org.m.courses.api.v1.user;

import org.junit.jupiter.api.Test;
import org.m.courses.api.v1.common.AbstractControllerTest;
import org.m.courses.api.v1.controller.user.UserRequest;
import org.m.courses.api.v1.controller.user.UserResponse;
import org.m.courses.builder.UserBuilder;
import org.m.courses.model.User;
import org.m.courses.service.UserService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class UserControllerTest extends AbstractControllerTest<User, UserRequest, UserResponse> {

    @MockBean
    private UserService userService;

    @Test
    void getTest() throws Exception {
        when(userService.get( anyLong() ))
                .thenReturn( UserBuilder.builder().buildNew() );

        mockMvc.perform(get( getControllerPath() ))
                .andDo(print())
                .andExpect( status().isOk() );
    }

    @Override
    protected String getControllerPath() {
        return "/api/v1/users/";
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    protected User getNewEntity() {
        return UserBuilder.builder().build();
    }

    @Override
    protected UserResponse convertToResponse(User user) {
        return new UserResponse( user );
    }

    @Override
    protected UserRequest convertToRequest(User user) {
        return new UserRequest( user );
    }

    @Override
    protected List<Function<User, Object>> getValueToBeUpdated() {
        List<Function<User, Object>> valuesToBeUpdated = new ArrayList<>();

        valuesToBeUpdated.add(User::getFirstName);
        valuesToBeUpdated.add(User::getLastName);
        valuesToBeUpdated.add(User::getPhoneNumber);
        valuesToBeUpdated.add(User::getLogin);
        valuesToBeUpdated.add(User::getPassword);

        return valuesToBeUpdated;
    }

    @Override
    protected Map<Consumer<UserRequest>, String> getCreateWithWrongValuesTestParameters() {
        Map<Consumer<UserRequest>, String> wrongValues = new HashMap<>();

        wrongValues.put( userRequest -> userRequest.setFirstName(null), "" );
//        wrongValues.put( userRequest -> userRequest.setFirstName(null), "" );
//        wrongValues.put( userRequest -> userRequest.setFirstName(null), "" );
        return wrongValues;
    }

    @Override
    protected UserService getService() {
        return userService;
    }
}
