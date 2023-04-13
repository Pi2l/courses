package org.m.courses.api.v1.controller.course;

import org.m.courses.api.v1.controller.common.AbstractResponse;
import org.m.courses.api.v1.controller.user.UserResponse;
import org.m.courses.model.Course;
import org.m.courses.model.User;

public class CourseResponse extends AbstractResponse {

    private UserResponse teacher;

    private String name;

    private String description;

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
}
