package br.com.alura.AluraFake.course.dto;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.Status;

import java.time.LocalDateTime;

public record CourseResponseDTO(
        Long id,
        String title,
        String description,
        String instructorEmail,
        Status status,
        LocalDateTime createdAt
) {
    public CourseResponseDTO(Course course) {
        this(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getInstructor().getEmail(),
                course.getStatus(),
                course.getCreatedAt()
        );
    }
}