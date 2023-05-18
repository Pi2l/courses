package org.m.courses.api.v1.controller.authorization;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

public class AuthenticationRequest {

    @Schema(description = "User login", example = "user")
    @NotBlank
    private String login;

    @Schema(description = "User password", example = "password")
    @NotBlank
    private String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
