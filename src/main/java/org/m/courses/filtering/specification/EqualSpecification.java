package org.m.courses.filtering.specification;

import org.m.courses.filtering.FilteringOperation;
import org.m.courses.filtering.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class EqualSpecification<Entity> implements Specification<Entity> {

    protected SearchCriteria criteria;

    public EqualSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<Entity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        switch (criteria.getOperation()) {
            case EQUAL:
                return criteriaBuilder.equal( root.get(criteria.getKey()), criteria.getValue() );
            case NOT_EQUAL:
                return criteriaBuilder.notEqual( root.get( criteria.getKey() ), criteria.getValue() );
            default:
                return null;
        }

    }
}
