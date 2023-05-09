package org.m.courses.filtering.specification;

import org.m.courses.filtering.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class ZonedDateTimeSpecificationBuilder<Entity> implements SpecificationBuilder<Entity> {

    @Override
    public Specification<Entity> buildSpecification(SearchCriteria criteria) {
        return new ZonedDateTimeComparisonSpecification<>( criteria );
    }
}
