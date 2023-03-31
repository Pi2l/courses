package org.m.courses.api.v1.controller.common;


import org.m.courses.exception.ItemNotFoundException;
import org.m.courses.model.Identity;
import org.m.courses.service.AbstractService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractController<
        Entity extends Identity<Long>,
        Request extends AbstractRequest<Entity>,
        Response extends AbstractResponse
        > {

    @GetMapping
    public List<Response> getAll() {
        List<Entity> entities = getService().getAll();

        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable Long id) {
        Entity entity = getEntity(id);
        return convertToResponse( entity );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response create(@RequestBody Request requestBody) {
        Entity createdEntity = createEntity( requestBody.createEntity() );

        return convertToResponse( createdEntity );
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Response update(@PathVariable Long id, @RequestBody Request requestBody) {
        Entity entity = getEntity(id);

        Entity updatedEntity = updateEntity( requestBody.updateEntity(entity) );
        return convertToResponse( updatedEntity );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        getService().delete( id );
    }

    private Entity getEntity(Long id) {
        Entity entity = getService().get(id);
        if (entity == null) {
            throw new ItemNotFoundException(id);
        }
        return entity;
    }

    protected Entity updateEntity(Entity entity) {
        return getService().update( entity );
    }

    protected Entity createEntity(Entity entity) {
        return getService().create( entity );
    }

    protected abstract Response convertToResponse(Entity entity);

    protected abstract AbstractService<Entity> getService();

}
