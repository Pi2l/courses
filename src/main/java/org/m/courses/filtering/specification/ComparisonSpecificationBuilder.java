package org.m.courses.filtering.specification;

import org.m.courses.filtering.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class ComparisonSpecificationBuilder<Entity, T extends Comparable<T>> implements SpecificationBuilder<Entity> {

    @Override
    public Specification<Entity> buildSpecification(SearchCriteria criteria) {
        return new ComparisonSpecification<Entity, T>( criteria );
    }
}
