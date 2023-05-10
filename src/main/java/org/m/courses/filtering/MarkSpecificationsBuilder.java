package org.m.courses.filtering;

import org.m.courses.filtering.specification.ComparisonSpecificationBuilder;
import org.m.courses.filtering.specification.EqualSpecificationBuilder;
import org.m.courses.model.Mark;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.m.courses.filtering.FilteringOperation.*;

@Component
public class MarkSpecificationsBuilder extends EntitySpecificationsBuilder<Mark> {

    public static List<FilterableProperty<Mark>> filterableProperties =
        List.of(
            new FilterableProperty<>("courseId", new EqualSpecificationBuilder<>(), Long.class, List.of(EQUAL, NOT_EQUAL)),
            new FilterableProperty<>("userId", new EqualSpecificationBuilder<>(), Long.class, List.of(EQUAL, NOT_EQUAL)),
            new FilterableProperty<>("value", new ComparisonSpecificationBuilder<>(), Integer.class,
                        List.of(GREATER_THEN, GREATER_OR_EQUAL, LESS_THEN, LESS_OR_EQUAL, EQUAL, NOT_EQUAL))
            );

    @Override
    public List<FilterableProperty<Mark>> getFilterableProperties() {
        return filterableProperties;
    }
}
