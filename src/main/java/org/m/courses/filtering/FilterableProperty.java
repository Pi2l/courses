package org.m.courses.filtering;

import org.m.courses.filtering.specification.SpecificationBuilder;

import java.util.List;

public class FilterableProperty<Entity> {

    private final String propertyName;

    private final SpecificationBuilder<Entity> specificationBuilder;

    private final Class< ? > expectedType;

    private final List<FilteringOperation> supportedOperations;

    public FilterableProperty(String propertyName,
                              SpecificationBuilder<Entity> builder,
                              Class< ? > expectedType,
                              List<FilteringOperation> supportedOperations) {
        this.propertyName = propertyName;
        this.specificationBuilder = builder;
        this.expectedType = expectedType;
        this.supportedOperations = supportedOperations;
    }


    public String getPropertyName() {
        return propertyName;
    }

    public SpecificationBuilder<Entity> getSpecificationBuilder() {
        return specificationBuilder;
    }

    public Class<?> getExpectedType() {
        return expectedType;
    }

    public List<FilteringOperation> getSupportedOperations() {
        return supportedOperations;
    }
}
