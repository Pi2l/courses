package org.m.courses.filtering.specification;

import org.m.courses.filtering.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class ContainSpecification<Entity> implements Specification<Entity> {

    private SearchCriteria criteria;

    public ContainSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<Entity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        switch (criteria.getOperation()) {
            case EQUAL:
                return criteriaBuilder.equal( root.get(criteria.getKey()), criteria.getValue() );
            case NOT_EQUAL:
                return criteriaBuilder.notEqual( root.get( criteria.getKey() ), criteria.getValue() );
            case CONTAIN:
                return criteriaBuilder.like( root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
            default:
                return null;
        }

    }
}
