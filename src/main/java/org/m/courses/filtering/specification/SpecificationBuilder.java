package org.m.courses.filtering.specification;

import org.m.courses.filtering.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;


public interface SpecificationBuilder<EntityType> {

    Specification<EntityType> buildSpecification(SearchCriteria criteria);

}
