package org.m.courses.api.v1.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.m.courses.api.v1.controller.common.AbstractRequest;
import org.m.courses.api.v1.controller.common.AbstractResponse;
import org.m.courses.exception.ItemNotFoundException;
import org.m.courses.model.Identity;
import org.m.courses.model.User;
import org.m.courses.service.AbstractService;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
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
        whenGetEntity(anyLong(), entity);

        Response expectedEntity = convertToResponse(entity);

        mockMvc.perform( get( getControllerPath() + entity.getId() )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( getJson(expectedEntity) ) );
    }

    @Test
    void getNotExistingEntityByIdTest() throws Exception {
        Entity entity = getNewEntity();
        whenGetEntity(anyLong(), null);

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
    public void createInvalidEntityTest() {
        mockServiceCreateOrUpdateMethod( resultCaptor, whenCreateInService( any( getEntityClass() ) ) );

        getCreateWithWrongValuesTestParameters().forEach( this::doCreateWithWrongValuesTestParameters );
    }

    private void doCreateWithWrongValuesTestParameters(Consumer<Request> invalidValue, Pair<String, String> errorMsg) {
        Request request = convertToRequest(getNewEntity());
        invalidValue.accept(request);
        try {
            mockMvc.perform(post(getControllerPath())
                            .content(getJson(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(jsonPath("$." + errorMsg.getFirst()).value(errorMsg.getSecond()))
                    .andExpect(status().isNotAcceptable());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void updateInvalidEntityTest() {
        mockServiceCreateOrUpdateMethod( resultCaptor, whenUpdateInService( any( getEntityClass() ) ) );

        getUpdateWithWrongValuesTestParameters().forEach( this::doUpdateWithWrongValuesTestParameters );
    }

    private void doUpdateWithWrongValuesTestParameters(Consumer<Request> invalidValue, Pair<String, String> errorMsg) {
        Entity entity = getNewEntity();
        Request request = convertToRequest( entity );
        invalidValue.accept(request);

        try {
            mockMvc.perform( put( getControllerPath() + "/{id}", entity.getId() )
                            .content(getJson(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect( jsonPath("$." + errorMsg.getFirst()).value(errorMsg.getSecond()) )
                    .andExpect( status().isNotAcceptable() );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void patchEntityTest() {
        getPatchValuesTestParameters().forEach( this::doPatchValuesTestParameters );
    }

    private void doPatchValuesTestParameters(Map<String, Object> requestedMap, List<Pair<Function<Entity, Object>, Object>> valueProviders) {
        Entity entity = getNewEntity();
        whenGetEntity( any( Long.class ), entity );

        mockServiceCreateOrUpdateMethod( resultCaptor, whenUpdateInService( any( getEntityClass() ) ) );

        try {
            mockMvc.perform( patch( getControllerPath() + "/{id}", entity.getId() )
                            .content( getJson(requestedMap) )
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect( status().isNoContent() );

            valueProviders.forEach( valueProvider -> {
                assertEquals(valueProvider.getSecond(), valueProvider.getFirst().apply( resultCaptor.getResult() ));
            });

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void patchInvalidEntityTest() {
        getPatchInvalidValuesTestParameters().forEach( this::doPatchInvalidValuesTestParameters );
    }

    private void doPatchInvalidValuesTestParameters(Map<String, Object> requestedMap, List<Pair<String, Object>> valueProviders) {
        Entity entity = getNewEntity();
        whenGetEntity( any( Long.class ), entity );

        mockServiceCreateOrUpdateMethod( resultCaptor, whenUpdateInService( any( getEntityClass() ) ) );


        valueProviders.forEach( valueProvider -> {

            try {
                mockMvc.perform( patch( getControllerPath() + "/{id}", entity.getId() )
                                .content( getJson(requestedMap) )
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect( jsonPath("$." + valueProvider.getFirst())
                                .value(valueProvider.getSecond()) )
                        .andExpect( status().isNotAcceptable() );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
    }

    @Test
    public void updateEntity() throws Exception {
        Entity entity = getNewEntity();

        Entity entityWithNewValues = getNewEntity();
        entityWithNewValues.setId( entity.getId() );

        Request request = convertToRequest( entityWithNewValues );

        List<Function< Entity, Object >> valuesGetters = getValueToBeUpdated();

        whenGetEntity(entity.getId(), entity);

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

    private void whenGetEntity(Long id, Entity entity) {
        when( getService().get(id) )
                .thenReturn(entity);
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

    protected abstract Map< Consumer< Request >, Pair< String, String > > getCreateWithWrongValuesTestParameters();

    protected abstract Map< Consumer< Request >, Pair< String, String > > getUpdateWithWrongValuesTestParameters();

    protected abstract Map< Map<String, Object>, List< Pair<Function<Entity, Object>, Object> > > getPatchValuesTestParameters();

    protected abstract Map< Map<String, Object>, List< Pair<String, Object> > > getPatchInvalidValuesTestParameters();
}
