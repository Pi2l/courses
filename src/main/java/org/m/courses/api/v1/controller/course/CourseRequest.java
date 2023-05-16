package org.m.courses.api.v1.controller.course;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.Range;
import org.m.courses.api.v1.controller.common.AbstractRequest;
import org.m.courses.model.Course;

import javax.validation.constraints.NotBlank;

public class CourseRequest extends AbstractRequest<Course> {

    @Schema(description = "User id", example = "1", nullable = true)
    private Long teacherId;

    @Schema(description = "Name", example = "name1")
    @NotBlank
    private String name;

    @Schema(description = "Description", example = "course description", nullable = true)
    private String description;

    @Schema(description = "Lesson count", example = "10", minimum = "1", nullable = true)
    @Range(min = 1)
    private Integer lessonCount;

    @Override
    public Course createEntity() {
        Course course = new Course();
        course.setTeacher( null );
        return updateEntity(course);
    }

    @Override
    public Course updateEntity(Course course) {
        course.setName( getName() );
        course.setDescription( getDescription() );
        course.setLessonCount( getLessonCount() );
        return course;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLessonCount() {
        return lessonCount;
    }

    public void setLessonCount(Integer lessonCount) {
        this.lessonCount = lessonCount;
    }
}
