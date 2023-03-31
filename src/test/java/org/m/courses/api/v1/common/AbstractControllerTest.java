package org.m.courses.api.v1.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.m.courses.api.v1.controller.common.AbstractRequest;
import org.m.courses.api.v1.controller.common.AbstractResponse;
import org.m.courses.exception.ItemNotFoundException;
import org.m.courses.model.Identity;
import org.m.courses.service.AbstractService;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public abstract class AbstractControllerTest<
        Entity extends Identity<Long>,
        Request extends AbstractRequest<Entity>,
        Response extends AbstractResponse
        > {

    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ResultCaptor<Entity> resultCaptor;

    @BeforeEach
    public void setUp() {
        resultCaptor = new ResultCaptor<>();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    protected abstract AbstractService<Entity> getService();

    protected abstract String getControllerPath();

    protected abstract Class<Entity> getEntityClass();

    protected abstract Entity getNewEntity();

    @Test
    void getExistingEntityByIdTest() throws Exception {
        Entity entity = getNewEntity();
        when( getService().get( anyLong() ) )
                .thenReturn(entity);

        Response expectedEntity = convertToResponse(entity);

        mockMvc.perform( get( getControllerPath() + entity.getId() )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( getJson(expectedEntity) ) );
    }

    @Test
    void getNotExistingEntityByIdTest() throws Exception {
        Entity entity = getNewEntity();
        when( getService().get( anyLong() ) )
                .thenReturn(null);

        mockMvc.perform( get( getControllerPath() + "/{id}", entity.getId() )
                        .accept( MediaType.APPLICATION_JSON ))
                .andExpect( status().isNotFound() );
    }

    @Test
    void getAllEntitiesTest() throws Exception {
        List<Entity> entities = List.of(getNewEntity(), getNewEntity());

        when( getService().getAll() )
                .thenReturn( entities );

        List<Response> expectedResponse = entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        mockMvc.perform( get( getControllerPath() )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( getJson(expectedResponse) ) );
    }

    @Test
    public void createEntityTest() throws Exception {

        mockServiceCreateOrUpdateMethod( resultCaptor, whenCreateInService( any( getEntityClass() ) ) );

        ResultActions resultAction = mockMvc.perform( post( getControllerPath() )
                        .content( getJson( convertToRequest( getNewEntity() ) ) )
                        .contentType( MediaType.APPLICATION_JSON )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isCreated() );

        assertNotNull( resultCaptor.getResult() );

        Response expectedResponse = convertToResponse( resultCaptor.getResult() );
        resultAction.andExpect( content().json( getJson( expectedResponse ) ) );
    }

    @Test
    public void updateEntity() throws Exception {
        Entity entity = getNewEntity();

        Entity entityWithNewValues = getNewEntity();
        entityWithNewValues.setId( entity.getId() );

        Request request = convertToRequest( entityWithNewValues );

        List<Function< Entity, Object >> valuesGetters = getValueToBeUpdated();

        when( getService().get( entity.getId() ) )
                .thenReturn( entity );

        mockServiceCreateOrUpdateMethod( resultCaptor, whenUpdateInService( any( getEntityClass() ) ) );

        ResultActions resultAction = mockMvc.perform( put( getControllerPath() + "/{id}", entity.getId() )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( getJson( request ) ) )
                .andExpect( status().isNoContent() );

        Entity updatedEntity = resultCaptor.getResult();

        Response expectedResponse = convertToResponse( updatedEntity );
        resultAction.andExpect( content().json( getJson( expectedResponse ) ) );

        for ( Function< Entity, Object > getter : valuesGetters ) {
            assertEquals( getter.apply( entityWithNewValues ), getter.apply( updatedEntity ) );
        }
    }

    @Test
    public void updateNotExistingEntity() throws Exception {
        Entity entity = getNewEntity();

        Entity entityWithNewValues = getNewEntity();
        entityWithNewValues.setId( entity.getId() );

        Request request = convertToRequest( entityWithNewValues );

        whenUpdateInService( any( getEntityClass() ) )
                .thenThrow( ItemNotFoundException.class );

        mockMvc.perform( put( getControllerPath() + "/{id}", entity.getId() )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( getJson( request ) ) )
                .andExpect( status().isNotFound() );
    }

    private OngoingStubbing<Entity> whenUpdateInService(Entity entity) {
        return when( getService().update(entity) );
    }

    private OngoingStubbing<Entity> whenCreateInService(Entity entity) {
        return when( getService().create(entity) );
    }

    protected <T> void mockServiceCreateOrUpdateMethod( ResultCaptor< Entity > resultCaptor, OngoingStubbing<T> ongoingStubbing){
        ongoingStubbing.thenAnswer( invocation -> {
            Entity entity = invocation.getArgument( 0, getEntityClass() );
            entity.setId( getRandomId() );
            resultCaptor.setResult(entity);
            return entity;});
    }

    protected String getJson(Object expectedEntity) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString( expectedEntity );
    }

    @Test
    void deleteEntityTest() throws Exception {
        Entity entity = getNewEntity();

        mockMvc.perform( delete( getControllerPath() + "/{id}", entity.getId() )
                        .accept( MediaType.APPLICATION_JSON ))
                .andExpect( status().isNoContent() );

        verify( getService(), times( 1 ) ).delete( eq( entity.getId() ) );
    }

    @Test
    void deleteEntityFailTest() throws Exception {
        Entity entity = getNewEntity();

        Mockito.doThrow(new ItemNotFoundException( entity.getId() ))
                .when(getService()).delete( anyLong() );

        mockMvc.perform( delete( getControllerPath() + "/{id}", entity.getId() )
                        .accept( MediaType.APPLICATION_JSON ))
                .andDo( print() )
                .andExpect( status().isNotFound() );

        verify( getService(), times( 1 ) ).delete( eq( entity.getId() ) );
    }

    protected abstract Response convertToResponse(Entity entity);

    protected abstract Request convertToRequest(Entity entity);

    protected Long getRandomId() {
        return ThreadLocalRandom.current().nextLong(Long.MAX_VALUE - 1);
    }

    protected abstract List<Function<Entity, Object>> getValueToBeUpdated();

    protected abstract Map< Consumer< Request >, String > getCreateWithWrongValuesTestParameters();
}
