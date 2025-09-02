package br.com.alura.AluraFake.report.dto;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.Status;

import java.time.LocalDateTime;

public record CourseReportItemDTO(
        Long id,
        String title,
        Status status,
        LocalDateTime publishedAt,
        int activityCount
) {
    public CourseReportItemDTO(Course course) {
        this(
                course.getId(),
                course.getTitle(),
                course.getStatus(),
                course.getPublishedAt(),
                course.getTasks().size()
        );
    }
}