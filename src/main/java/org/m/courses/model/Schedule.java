package org.m.courses.model;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "schedule")
public class Schedule implements Identity<Long> {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Course course;

    @Column(name = "start_at", nullable = false)
    private ZonedDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private ZonedDateTime endAt;

    @ManyToOne(optional = false)
    private Group group;

    @Column(nullable = false, name = "time_zone")
    private String timeZone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public ZonedDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(ZonedDateTime startAt) {
        this.startAt = startAt;
    }

    public ZonedDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(ZonedDateTime endAt) {
        this.endAt = endAt;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule user = (Schedule) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
