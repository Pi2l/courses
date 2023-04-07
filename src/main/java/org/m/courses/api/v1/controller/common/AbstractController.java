package org.m.courses.api.v1.controller.common;


import org.hibernate.validator.constraints.Range;
import org.m.courses.exception.IllegalFilteringOperationException;
import org.m.courses.exception.ItemNotFoundException;
import org.m.courses.filtering.EntitySpecificationsBuilder;
import org.m.courses.filtering.FilterableProperty;
import org.m.courses.filtering.FilteringOperation;
import org.m.courses.filtering.SearchCriteria;
import org.m.courses.model.Identity;
import org.m.courses.service.AbstractService;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.groups.Default;
import javax.websocket.server.PathParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Validated
public abstract class AbstractController<
        Entity extends Identity<Long>,
        Request extends AbstractRequest<Entity>,
        Response extends AbstractResponse
        > {

    private static final Pattern PATTERN = Pattern.compile("(\\w+?)(:|!_=|[!<>_]=?|=)(.*)");

    @GetMapping
    public PageResponse<Response> getAll(
            @RequestParam(defaultValue = "0", required = false) @PositiveOrZero Integer index,
            @RequestParam(defaultValue = "30", required = false) @Range(max = 100) Integer size,
            @PathParam(value = "sort") Sort sort,
            @PathParam(value = "filter") String filter
            ) {
        sort = mapSortProperties( sort );

        Specification<Entity> specification = null;
        if (filter != null) {
            specification = getSpecificationFromFilter(filter);
        }

        Pageable pageable = PageRequest.of(index, size, sort);
        Page<Entity> entityPage = getService().getAll( pageable, specification );
        List<Response> responses = entityPage.getContent().stream()
                .map( this::convertToResponse ).collect(Collectors.toList());

        return new PageResponse<>(responses, entityPage.getTotalElements(), entityPage.getNumber(), entityPage.getSize());
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable Long id) {
        Entity entity = getEntity(id);
        return convertToResponse( entity );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response create(@Validated({ CreateValidationGroup.class, Default.class }) @RequestBody Request requestBody) {
        Entity createdEntity = createEntity( requestBody.createEntity() );

        return convertToResponse( createdEntity );
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Response update(@PathVariable Long id, @Validated({ UpdateValidationGroup.class, Default.class }) @RequestBody Request requestBody) {
        Entity entity = getEntity(id);

        Entity updatedEntity = updateEntity( requestBody.updateEntity(entity) );
        return convertToResponse( updatedEntity );
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Response patch(@PathVariable Long id, @RequestBody Map<String, Object> requestBody) {
        Entity entity = getEntity(id);

        entity = patchRequest( requestBody, entity );

        Entity updatedEntity = patchEntity( entity );
        return convertToResponse( updatedEntity );
    }

    protected abstract Entity patchRequest(Map<String, Object> requestBody, Entity entity);

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

    protected Entity patchEntity(Entity entity) {
        return getService().update( entity );
    }

    protected Entity createEntity(Entity entity) {
        return getService().create( entity );
    }

    protected Sort mapSortProperties(Sort sort ) {
        if ( sort == null ) {
            return Sort.unsorted();
        }

        return sort;
    }

    protected Specification<Entity> getSpecificationFromFilter(String filter) {
        String [] filtersByField = filter.split(",");
        List<SearchCriteria> searchCriteria = new ArrayList<>();
        EntitySpecificationsBuilder<Entity> specificationsBuilder = getSpecificationBuilder();

        for (String filterByField : filtersByField) {
            Matcher matcher = PATTERN.matcher(filterByField);
            while (matcher.find()) {
                String key = matcher.group(1);
                String operationStr = matcher.group(2);
                FilteringOperation operation = FilteringOperation.fromString(operationStr);
                String value = matcher.group(3);

                Optional< FilterableProperty< Entity > > filterableProperty =
                        specificationsBuilder.getFilterableProperties().stream()
                            .filter( property -> property.getPropertyName().equals( key ) ).findFirst();

                if (filterableProperty.isPresent()) {
                    if (!filterableProperty.get().getSupportedOperations().contains(operation)) {
                        throw new IllegalFilteringOperationException("Operation '" + operation + "' is not supported for property " + key);
                    }

                    Object convertedValue;
                    if ("null".equals(value) || (value != null && value.isBlank())) {
                        convertedValue = null;
                    } else {
                        convertedValue = convertValueForCriteria( value, filterableProperty.get() );
                    }

                    searchCriteria.add(new SearchCriteria(key, operation, convertedValue));
                }
            }
        }
        return specificationsBuilder.buildSpecification(searchCriteria);
    }

    protected Object convertValueForCriteria( String value, FilterableProperty< Entity > filterableProperty ) {
        return getConversionService().convert( value, filterableProperty.getExpectedType() );
    }

    protected abstract ConversionService getConversionService();

    protected abstract EntitySpecificationsBuilder<Entity> getSpecificationBuilder();

    protected abstract Response convertToResponse(Entity entity);

    protected abstract AbstractService<Entity> getService();

}
