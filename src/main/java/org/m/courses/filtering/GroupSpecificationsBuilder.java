package org.m.courses.filtering;

import org.m.courses.filtering.specification.ContainSpecificationBuilder;
import org.m.courses.filtering.specification.EqualSpecificationBuilder;
import org.m.courses.model.Group;
import org.m.courses.model.Role;
import org.m.courses.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.m.courses.filtering.FilteringOperation.*;

@Component
public class GroupSpecificationsBuilder extends EntitySpecificationsBuilder<Group> {

    public static List<FilterableProperty<Group>> filterableProperties =
        List.of(
            new FilterableProperty<>("name", new ContainSpecificationBuilder<>(), String.class, List.of(CONTAIN, EQUAL, NOT_EQUAL))
            );

    @Override
    public List<FilterableProperty<Group>> getFilterableProperties() {
        return filterableProperties;
    }
}
