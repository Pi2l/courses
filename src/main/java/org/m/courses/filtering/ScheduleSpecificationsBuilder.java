package org.m.courses.filtering;

import org.m.courses.filtering.specification.EqualSpecificationBuilder;
import org.m.courses.filtering.specification.ZonedDateTimeSpecificationBuilder;
import org.m.courses.model.Schedule;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

import static org.m.courses.filtering.FilteringOperation.*;

@Component
public class ScheduleSpecificationsBuilder extends EntitySpecificationsBuilder<Schedule> {

    public static List<FilterableProperty<Schedule>> filterableProperties =
        List.of(
            new FilterableProperty<>("courseId", new EqualSpecificationBuilder<>(), Long.class, List.of(EQUAL, NOT_EQUAL)),
            new FilterableProperty<>("startAt", new ZonedDateTimeSpecificationBuilder<>(), ZonedDateTime.class,
                    List.of(GREATER_THEN, GREATER_OR_EQUAL, LESS_THEN, LESS_OR_EQUAL, EQUAL, NOT_EQUAL)),
            new FilterableProperty<>("endAt", new ZonedDateTimeSpecificationBuilder<>(), ZonedDateTime.class,
                    List.of(GREATER_THEN, GREATER_OR_EQUAL, LESS_THEN, LESS_OR_EQUAL, EQUAL, NOT_EQUAL)),
            new FilterableProperty<>("groupId", new EqualSpecificationBuilder<>(), Long.class, List.of(EQUAL, NOT_EQUAL))
            );

    @Override
    public List<FilterableProperty<Schedule>> getFilterableProperties() {
        return filterableProperties;
    }
}
