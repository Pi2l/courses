package org.m.courses.filtering.specification;

import org.m.courses.filtering.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class EqualOrNullSpecificationBuilder<Entity> implements SpecificationBuilder<Entity> {

    @Override
    public Specification<Entity> buildSpecification(SearchCriteria criteria) {
        return new EqualOrNullSpecification<>( criteria );
    }
}
