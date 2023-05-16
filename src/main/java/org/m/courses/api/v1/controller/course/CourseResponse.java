package org.m.courses.api.v1.controller.course;

import io.swagger.v3.oas.annotations.media.Schema;
import org.m.courses.api.v1.controller.common.AbstractResponse;
import org.m.courses.api.v1.controller.user.UserResponse;
import org.m.courses.model.Course;
import org.m.courses.model.User;

public class CourseResponse extends AbstractResponse {

    private UserResponse teacher;

    @Schema(description = "Name", example = "name1")
    private String name;

    @Schema(description = "Description", example = "course description", nullable = true)
    private String description;

    @Schema(description = "Lesson count", example = "10", minimum = "1", nullable = true)
    private Integer lessonCount;

    public CourseResponse(Course course) {
        super( course.getId() );

        User courseTeacher = course.getTeacher();
        if (courseTeacher != null) {
            this.teacher = new UserResponse(courseTeacher);
        }
        this.name = course.getName() ;
        this.description = course.getDescription();
        this.lessonCount = course.getLessonCount();
    }

    public UserResponse getTeacher() {
        return teacher;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getLessonCount() {
        return lessonCount;
    }
}
