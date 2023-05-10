package org.m.courses.builder;

import org.m.courses.dao.MarkDao;
import org.m.courses.model.Course;
import org.m.courses.model.Mark;
import org.m.courses.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;


@Component
public class MarkBuilder {

    @Autowired private MarkDao markDao;

    private Mark mark;

    public static MarkBuilder builder() {
        return new MarkBuilder();
    }

    public MarkBuilder() {
        initDefaultUser();
    }

    public Mark build() {
        Mark odlEntity = mark;
        initDefaultUser();
        return odlEntity;
    }

    public Mark buildNew() {
        return setId(null)
                .build();
    }

    // Spring based
    public Mark toDB() {
        return markDao.create( buildNew() );
    }

    private MarkBuilder initDefaultUser() {
        long randomValue = Math.abs(new SecureRandom().nextLong()) % 1000000;
        this.mark = new Mark();

        setId(randomValue);
        setValue((int) (randomValue % 101));
        return this;
    }

    public MarkBuilder setId(Long id) {
        mark.setId(id);
        return this;
    }

    public MarkBuilder setCourse(Course course) {
        mark.setCourse(course);
        return this;
    }

    public MarkBuilder setUser(User user) {
        mark.setUser( user );
        return this;
    }

    public MarkBuilder setValue(Integer value) {
        mark.setValue(value);
        return this;
    }
}
