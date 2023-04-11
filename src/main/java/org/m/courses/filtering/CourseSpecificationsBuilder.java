package org.m.courses.filtering;

import org.m.courses.filtering.specification.ComparisonSpecificationBuilder;
import org.m.courses.filtering.specification.ContainSpecificationBuilder;
import org.m.courses.filtering.specification.EqualOrNullSpecificationBuilder;
import org.m.courses.filtering.specification.EqualSpecificationBuilder;
import org.m.courses.model.Course;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.m.courses.filtering.FilteringOperation.*;

@Component
public class CourseSpecificationsBuilder extends EntitySpecificationsBuilder<Course> {

    public static List<FilterableProperty<Course>> filterableProperties =
        List.of(
            new FilterableProperty<>("teacher", new EqualOrNullSpecificationBuilder<>(), Long.class, List.of( EQUAL, NOT_EQUAL, IS_NULL, IS_NOT_NULL)),
            new FilterableProperty<>("name", new ContainSpecificationBuilder<>(), String.class, List.of(CONTAIN, EQUAL, NOT_EQUAL)),
            new FilterableProperty<>("description", new ContainSpecificationBuilder<>(), String.class, List.of(CONTAIN, EQUAL, NOT_EQUAL)),
            new FilterableProperty<>("lessonCount", new ComparisonSpecificationBuilder<>(), Integer.class,
                    List.of(GREATER_THEN, GREATER_OR_EQUAL, LESS_THEN, LESS_OR_EQUAL, EQUAL, NOT_EQUAL))
            );

    @Override
    public List<FilterableProperty<Course>> getFilterableProperties() {
        return filterableProperties;
    }
}
