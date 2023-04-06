package org.m.courses.filtering;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.jpa.domain.Specification.where;

public abstract class EntitySpecificationsBuilder<EntityType> {

    public abstract List<FilterableProperty<EntityType>> getFilterableProperties();

    public Specification<EntityType> buildSpecification(List<SearchCriteria> criteriaList) {
        Specification<EntityType> specification = null;

        for (SearchCriteria searchCriteria : criteriaList) {

            Optional< FilterableProperty<EntityType> > filterableProperty = getFilterableProperties().stream()
                    .filter( property -> property.getPropertyName().equals(searchCriteria.getKey())).findFirst();

            if (filterableProperty.isPresent()) {

                Specification<EntityType> andSpecification = filterableProperty.get()
                        .getSpecificationBuilder().buildSpecification( searchCriteria );

                if (specification == null) {
                    specification = where(andSpecification);
                } else {
                    specification.and(andSpecification);
                }
            }
        }
        return specification;
    }

}
