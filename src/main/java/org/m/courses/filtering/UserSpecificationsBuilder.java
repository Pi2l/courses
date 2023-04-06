package org.m.courses.filtering;

import org.m.courses.filtering.specification.EqualSpecificationBuilder;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.m.courses.filtering.FilteringOperation.EQUAL;
import static org.m.courses.filtering.FilteringOperation.NOT_EQUAL;

@Component
public class UserSpecificationsBuilder extends EntitySpecificationsBuilder<User> {

    public static List<FilterableProperty<User>> filterableProperties =
        List.of(
            new FilterableProperty<>("firstName", new EqualSpecificationBuilder<>(), String.class, List.of(EQUAL, NOT_EQUAL)),
            new FilterableProperty<>("lastName", new EqualSpecificationBuilder<>(), String.class, List.of(EQUAL, NOT_EQUAL)),
            new FilterableProperty<>("phoneNumber", new EqualSpecificationBuilder<>(), String.class, List.of(EQUAL, NOT_EQUAL)),
            new FilterableProperty<>("login", new EqualSpecificationBuilder<>(), String.class, List.of(EQUAL, NOT_EQUAL)),
            new FilterableProperty<>("role", new EqualSpecificationBuilder<>(), Role.class, List.of(EQUAL, NOT_EQUAL))
            );

    @Override
    public List<FilterableProperty<User>> getFilterableProperties() {
        return filterableProperties;
    }
}
