package org.m.courses.filtering.specification;

import org.m.courses.filtering.SearchCriteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class EqualOrNullSpecification<Entity> extends EqualSpecification<Entity> {

    public EqualOrNullSpecification(SearchCriteria criteria) {
        super(criteria);
    }

    @Override
    public Predicate toPredicate(Root<Entity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        switch (criteria.getOperation()) {
            case IS_NULL:
                return criteriaBuilder.isNull( root.get(criteria.getKey()) );
            case IS_NOT_NULL:
                return criteriaBuilder.isNotNull( root.get( criteria.getKey() ));
            default:
                return super.toPredicate(root, query, criteriaBuilder);
        }
    }
}
