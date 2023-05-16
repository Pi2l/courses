package org.m.courses.api.v1.controller.common;

import io.swagger.v3.oas.annotations.media.Schema;

public class AbstractResponse {

    @Schema(description = "Entity identifier", example = "1")
    private Long id;

    public AbstractResponse(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
