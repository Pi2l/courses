package org.m.courses.api.v1.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.m.courses.api.v1.controller.common.AbstractRequest;
import org.m.courses.api.v1.controller.common.AbstractResponse;
import org.m.courses.api.v1.controller.common.PageResponse;
import org.m.courses.exception.ItemNotFoundException;
import org.m.courses.filtering.EntitySpecificationsBuilder;
import org.m.courses.filtering.SearchCriteria;
import org.m.courses.model.Identity;
import org.m.courses.service.AbstractService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    public void getExistingEntityByIdTest() throws Exception {
        Entity entity = getNewEntity();
        whenGetEntity(anyLong(), entity);

        Response expectedEntity = convertToResponse(entity);

        mockMvc.perform( get( getControllerPath() + entity.getId() )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( getJson(expectedEntity) ) );
    }

    @Test
    public void getNotExistingEntityByIdTest() throws Exception {
        Entity entity = getNewEntity();
        whenGetEntity(anyLong(), null);

        mockMvc.perform( get( getControllerPath() + "/{id}", entity.getId() )
                        .accept( MediaType.APPLICATION_JSON ))
                .andExpect( status().isNotFound() );
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
        whenGetEntity(any(Long.class), entity);
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

    private void doPatchValuesTestParameters(Map<String, Object> requestedMap, Pair<Function<Entity, Object>, Object> valueProvider) {
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

            assertEquals(valueProvider.getSecond(), valueProvider.getFirst().apply( resultCaptor.getResult() ));

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void patchInvalidEntityTest() {
        getPatchInvalidValuesTestParameters().forEach( this::doPatchInvalidValuesTestParameters );
    }

    private void doPatchInvalidValuesTestParameters(Map<String, Object> requestedMap, Pair<String, Object> valueProvider) {
        Entity entity = getNewEntity();
        whenGetEntity( any( Long.class ), entity );

        mockServiceCreateOrUpdateMethod( resultCaptor, whenUpdateInService( any( getEntityClass() ) ) );

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
    }

    @Test
    public void createEntityWithOptionalFieldsTest() {
        mockServiceCreateOrUpdateMethod( resultCaptor, whenCreateInService( any( getEntityClass() ) ) );

        getCreateWithOptionalValuesTestParameters().forEach( this::doCreateWithOptionalValuesTestParameters );
    }

    private void doCreateWithOptionalValuesTestParameters(Consumer<Request> optionalValueSetter, Pair<Function<Entity, Object>, Object> valueProvider) {
        Entity entity = getNewEntity();

        whenGetEntity(any(Long.class), entity);
        Request request = convertToRequest( entity );

        optionalValueSetter.accept(request);

        try {
            ResultActions resultAction = mockMvc.perform( post(getControllerPath() )
                            .content(getJson(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isCreated());

            Entity updatedEntity = resultCaptor.getResult();

            Response expectedResponse = convertToResponse( updatedEntity );
            resultAction.andExpect( content().json( getJson( expectedResponse ) ) );

            assertEquals( valueProvider.getFirst().apply(updatedEntity), valueProvider.getSecond() );

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void updateEntityWithOptionalFieldsTest() {
        mockServiceCreateOrUpdateMethod( resultCaptor, whenCreateInService( any( getEntityClass() ) ) );

        getUpdateWithOptionalValuesTestParameters().forEach( this::doUpdateWithOptionalValuesTestParameters );
    }

    private void doUpdateWithOptionalValuesTestParameters(Consumer<Request> optionalValueSetter, Pair<Function<Entity, Object>, Object> valueProvider) {
        Entity entity = getNewEntity();
        Request request = convertToRequest( entity );
        whenGetEntity(any(Long.class), entity);

        optionalValueSetter.accept(request);

        try {
            ResultActions resultAction = mockMvc.perform( put(getControllerPath() + "/{id}", entity.getId() )
                            .content(getJson(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            Entity updatedEntity = resultCaptor.getResult();

            Response expectedResponse = convertToResponse( updatedEntity );
            resultAction.andExpect( content().json( getJson( expectedResponse ) ) );

            assertEquals( valueProvider.getFirst().apply(updatedEntity), valueProvider.getSecond() );

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
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
    public void getAllTest() throws Exception {
        List<Entity> entities = new LinkedList<>( Arrays.asList( getNewEntity(), getNewEntity() ) );

        Page<Entity> page = mockPage( entities );

        List<Response> expectedResponse = entities.stream().map( this :: convertToResponse ).collect( Collectors.toList() );

        PageResponse< Response > expectedResponses = new PageResponse<>(expectedResponse, page.getTotalElements(), page.getNumber(), page.getSize());
        mockMvc.perform( get( getControllerPath() )
                        .accept( MediaType.APPLICATION_JSON_VALUE ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( getJson( expectedResponses ) ) );
    }

    @Test
    public void getAllWithDefaultPageValuesTest() throws Exception {
        mockPage( List.of() );

        mockMvc.perform( get( getControllerPath() )
                        .accept( MediaType.APPLICATION_JSON_VALUE ) )
                .andExpect( status().isOk() );

        Pageable captor = getPageableArgumentCaptorValue();

        assertEquals(0, captor.getPageNumber());
        assertEquals(30, captor.getPageSize());
    }

    @Test
    public void getAllWithCustomPageValuesTest() throws Exception {
        mockPage( List.of() );

        mockMvc.perform( get( getControllerPath() )
                        .param("index", "7")
                        .param("size", "20")
                        .accept( MediaType.APPLICATION_JSON_VALUE ) )
                .andExpect( status().isOk() );

        Pageable pageable = getPageableArgumentCaptorValue();

        assertEquals(7, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    public void getAllWithCustomInvalidPageValuesTest() throws Exception {
        mockPage( List.of() );

        mockMvc.perform( get( getControllerPath() )
                        .param("index", "-1")
                        .accept( MediaType.APPLICATION_JSON_VALUE ) )
                .andExpect( jsonPath("$.['getAll.index']").value("must be greater than or equal to 0") )
                .andExpect( status().isNotAcceptable() );
        mockMvc.perform( get( getControllerPath() )
                        .param("size", "-1")
                        .accept( MediaType.APPLICATION_JSON_VALUE ) )
                .andExpect( jsonPath("$.['getAll.size']").value("must be between 0 and 100") )
                .andExpect( status().isNotAcceptable() );
        mockMvc.perform( get( getControllerPath() )
                        .param("size", "101")
                        .accept( MediaType.APPLICATION_JSON_VALUE ) )
                .andExpect( jsonPath("$.['getAll.size']").value("must be between 0 and 100") )
                .andExpect( status().isNotAcceptable() );
    }

    @Test
    public void getAllWithSortingTest() {
        mockPage(List.of());

        getSortingTestParams().forEach( this::doSortingParamTest );
    }

    private void doSortingParamTest(List<String> sortingValues, Sort expectedSort) {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get( getControllerPath() );
        sortingValues.forEach( value -> mockHttpServletRequestBuilder.param( "sort", value ));

        try {
            mockMvc.perform(mockHttpServletRequestBuilder)
                    .andExpect(status().isOk());
        } catch (Exception e) {
            fail();
        }

        Pageable pageable = getPageableArgumentCaptorValue();
        assertEquals( expectedSort, pageable.getSort() );
    }

    @Test
    public void getAllWithFilteringTest() {
        mockPage(List.of());

        getFilteringTestParams().forEach( this::doFilteringParamTest );
    }

    private void doFilteringParamTest(List<String> filter, List<SearchCriteria> providedCriteria) {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get( getControllerPath() );
        filter.forEach( value -> mockHttpServletRequestBuilder.param( "filter", value ));

        try {
            mockMvc.perform(mockHttpServletRequestBuilder)
                    .andExpect(status().isOk());
        } catch (Exception e) {
            fail();
        }

        Specification<Entity> specificationArgumentCaptor = getSpecificationArgumentCaptorValue();
        assertNotNull( specificationArgumentCaptor );

        List<SearchCriteria> searchCriteria = getSearchCriteriaArgumentCaptorValue();

        assertEquals( searchCriteria.size(), providedCriteria.size() );

        searchCriteria.forEach( criteria -> assertEqualsCriteria(providedCriteria, criteria ));
    }

    @Test
    public void getAllWithInvalidFilteringTest() {
        mockPage(List.of());

        getInvalidFilteringTestParams().forEach( this::doInvalidFilteringParamTest );
    }

    private void doInvalidFilteringParamTest(String filter, String providedValues) {

        try {
            mockMvc.perform( get( getControllerPath() )
                            .param("filter", filter) )
                    .andExpect(status().isOk());
        } catch (Exception e) {
            fail();
        }

        Specification<Entity> spec = getSpecificationArgumentCaptorValue();
        assertNull( spec );
        
        List<SearchCriteria> criteriaList = getSearchCriteriaArgumentCaptorValue();
        assertTrue( criteriaList.size() == 0 );
    }

    private void assertEqualsCriteria(List<SearchCriteria> providedSearchCriteria, SearchCriteria criteria) {
        Optional<SearchCriteria> providedCriteria = providedSearchCriteria.stream().filter(providedCriteria1 -> providedCriteria1.equals( criteria ) ).findFirst();
        assertTrue( providedCriteria.isPresent() );
    }

    private Pageable getPageableArgumentCaptorValue() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass( Pageable.class );
        verify( getService(), atLeastOnce() ).getAll( captor.capture(), any() );
        return captor.getValue();
    }

    private List<SearchCriteria> getSearchCriteriaArgumentCaptorValue() {
        ArgumentCaptor< List<SearchCriteria> > captor = ArgumentCaptor.forClass( List.class );

        verify( getEntitySpecificationsBuilder(), atLeastOnce() ).buildSpecification( captor.capture() );
        return captor.getValue();
    }

    private Specification<Entity> getSpecificationArgumentCaptorValue() {
        ArgumentCaptor< Specification<Entity> > specificationArgumentCaptor = ArgumentCaptor.forClass( Specification.class );
        verify( getService(), atLeastOnce() ).getAll( any(), specificationArgumentCaptor.capture() );
        return specificationArgumentCaptor.getValue();
    }

    private Page<Entity> mockPage( List< Entity > entities ) {
        Page<Entity> page = mock(Page.class);
        when( page.getContent()).thenReturn(entities);
        when( page.getTotalElements() ).thenReturn( 423L );
        when( page.getNumber() ).thenReturn( 1 );
        when( page.getSize() ).thenReturn( 5 );
        when( page.iterator() ).thenReturn( entities.iterator() );

        when( getService().getAll( any(Pageable.class), any() ) ).thenReturn(page);
        return page;
    }

    @Test
    public void deleteEntityTest() throws Exception {
        Entity entity = getNewEntity();

        mockMvc.perform( delete( getControllerPath() + "/{id}", entity.getId() )
                        .accept( MediaType.APPLICATION_JSON ))
                .andExpect( status().isNoContent() );

        verify( getService(), times( 1 ) ).delete( eq( entity.getId() ) );
    }

    @Test
    public void deleteEntityFailTest() throws Exception {
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

    protected abstract Map<Consumer<Request>, Pair<String, String>> getCreateWithWrongValuesTestParameters();

    protected abstract Map< Consumer< Request >, Pair< String, String > > getUpdateWithWrongValuesTestParameters();

    protected abstract Map< Map<String, Object>, Pair<Function<Entity, Object>, Object> > getPatchValuesTestParameters();

    protected abstract Map< Map<String, Object>, Pair<String, Object> > getPatchInvalidValuesTestParameters();

    protected Map< Consumer< Request >, Pair< Function<Entity, Object>, Object > > getCreateWithOptionalValuesTestParameters() {
        return new HashMap<>();
    }

    protected Map< Consumer< Request >, Pair< Function<Entity, Object>, Object > > getUpdateWithOptionalValuesTestParameters() {
        return new HashMap<>();
    }

    protected abstract Map< List<String>, Sort> getSortingTestParams();

    protected abstract Map< List<String>, List<SearchCriteria> > getFilteringTestParams();

    protected abstract Map< String, String > getInvalidFilteringTestParams();

    protected abstract EntitySpecificationsBuilder<Entity> getEntitySpecificationsBuilder();
}
