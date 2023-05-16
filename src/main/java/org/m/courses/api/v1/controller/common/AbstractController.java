package org.m.courses.api.v1.controller.common;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @ResponseBody
    @GetMapping
    @Operation(summary = "Get all entities", description = "Get all entities", tags = {"getAll"}, responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    }, parameters = {
            @Parameter(name = "index", description = "Page index. The default value is 0", example = "0"),
            @Parameter(name = "size", description = "Page size. The default value is 30", example = "10"),
            @Parameter(name = "sort", description = "Sort field. The default sorting is ascending", required = true, example = "{ \"sort\": [\n\"id,desc\"\n ] }"),
            @Parameter(name = "filter", description = "Filter field. The default is no filters",
                    example = "field1=value1,field2!=value2,field3:value3,field4>value4,field5>=value5,field6<value6,field7<=value7"),
    })
    public PageResponse<Response> getAll(
            @RequestParam(defaultValue = "0", required = false) @PositiveOrZero Integer index,
            @RequestParam(defaultValue = "30", required = false) @Range(max = 100) Integer size,
            @PathParam(value = "sort") Sort sort,
            @RequestParam(required = false, value = "filter") String filter
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

    @ResponseBody
    @GetMapping("/{id}")
    @Operation(summary = "Get entity by id", description = "Get entity by id", tags = {"get"}, responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Item not found"),
            @ApiResponse(responseCode = "406", description = "Not Acceptable"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    }, parameters = {
            @Parameter(name = "id", description = "Item id", example = "1", required = true),
    })
    public Response get(@PathVariable Long id) {
        Entity entity = getEntity(id);
        return convertToResponse( entity );
    }

    @ResponseBody
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create entity", description = "Create entity", tags = {"create"}, responses = {
            @ApiResponse(responseCode = "201", description = "Entity created"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "403", description = "Access denied for user"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Entity to create", required = true))
    public Response create(@Validated({ CreateValidationGroup.class, Default.class }) @RequestBody Request requestBody) {
        Entity createdEntity = createEntity( requestBody.createEntity(), requestBody );

        return convertToResponse( createdEntity );
    }

    @ResponseBody
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update entity", description = "Update entity", tags = {"update"}, responses = {
            @ApiResponse(responseCode = "204", description = "Entity updated"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "403", description = "Access denied for user"),
            @ApiResponse(responseCode = "404", description = "Item not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    }, parameters = {
            @Parameter(name = "id", description = "Item id", example = "1", required = true),
    }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Entity to update", required = true))
    public Response update(@PathVariable Long id, @Validated({ UpdateValidationGroup.class, Default.class }) @RequestBody Request requestBody) {
        Entity entity = getEntity(id);

        Entity updatedEntity = updateEntity( requestBody.updateEntity(entity), requestBody );
        return convertToResponse( updatedEntity );
    }

    @ResponseBody
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Patch entity", description = "Patch entity", tags = {"patch"}, responses = {
            @ApiResponse(responseCode = "204", description = "Entity patched"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "403", description = "Access denied for user"),
            @ApiResponse(responseCode = "404", description = "Item not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    }, parameters = {
            @Parameter(name = "id", description = "Item id", example = "1", required = true),
    }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Map with field, value to update", required = true))
    public Response patch(@PathVariable Long id, @RequestBody Map<String, Object> requestBody) {
        Entity entity = getEntity(id);

        entity = patchRequest( requestBody, entity );

        Entity updatedEntity = patchEntity( entity, requestBody );
        return convertToResponse( updatedEntity );
    }

    protected abstract Entity patchRequest(Map<String, Object> requestBody, Entity entity);

    @ResponseBody
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete entity", description = "Delete entity", tags = {"delete"}, responses = {
            @ApiResponse(responseCode = "204", description = "Entity deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied for user"),
            @ApiResponse(responseCode = "404", description = "Item not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    }, parameters = {
            @Parameter(name = "id", description = "Item id", example = "1", required = true),
    })
    public void delete(@PathVariable Long id) {
        getEntity(id);
        deleteEntity(id);
    }

    protected void deleteEntity(Long id) {
        getService().delete(id);
    }

    protected Entity getEntity(Long id) {
        Entity entity = getService().get(id);
        if (entity == null) {
            throw new ItemNotFoundException(id);
        }
        return entity;
    }

    protected Entity updateEntity(Entity entity, Request request) {
        return getService().update( entity );
    }

    protected Entity patchEntity(Entity entity, Map<String, Object> request) {
        return getService().update( entity );
    }

    protected Entity createEntity(Entity entity, Request request) {
        return getService().create( entity );
    }

    protected Sort mapSortProperties(Sort sort ) {
        if ( sort == null ) {
            return Sort.unsorted();
        }

        List<Sort.Order> sorts = sort.stream()
                .map(order -> new Sort.Order(order.getDirection(), order.getProperty(), order.getNullHandling()))
                .collect(Collectors.toList());
        return Sort.by( sorts );
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
