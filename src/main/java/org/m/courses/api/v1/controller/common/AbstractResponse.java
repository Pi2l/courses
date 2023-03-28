package org.m.courses.api.v1.controller.common;

public class AbstractResponse {

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
