package br.com.alura.AluraFake.report.dto;

import java.util.List;

public record InstructorCoursesReportDTO(
        List<CourseReportItemDTO> courses,
        long totalPublishedCourses
) {}