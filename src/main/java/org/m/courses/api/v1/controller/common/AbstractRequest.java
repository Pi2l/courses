package org.m.courses.api.v1.controller.common;

public abstract class AbstractRequest<Entity> {

    public abstract Entity createEntity();

    public abstract Entity updateEntity(Entity entity);

}
