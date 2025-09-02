package br.com.alura.AluraFake.report;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.infra.exception.BusinessRuleException;
import br.com.alura.AluraFake.report.dto.InstructorCoursesReportDTO;
import br.com.alura.AluraFake.user.Role;
import br.com.alura.AluraFake.user.User;
import br.com.alura.AluraFake.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ReportService reportService;

    private User instructor;

    @BeforeEach
    void setUp() {
        instructor = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR, "123456");
    }

    @Test
    void generateInstructorCoursesReport_should_returnReport_whenUserIsAValidInstructor() {
        Course course1 = new Course("Java Básico", "Descrição", instructor);
        course1.setStatus(Status.PUBLISHED); // Um curso publicado
        Course course2 = new Course("Spring Boot", "Descrição", instructor);
        course2.setStatus(Status.BUILDING); // Um curso em construção

        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByInstructor(instructor)).thenReturn(List.of(course1, course2));

        InstructorCoursesReportDTO report = reportService.generateInstructorCoursesReport(1L);

        assertThat(report).isNotNull();
        assertThat(report.courses()).hasSize(2);
        assertThat(report.totalPublishedCourses()).isEqualTo(1);
    }

    @Test
    void generateInstructorCoursesReport_should_throwEntityNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            reportService.generateInstructorCoursesReport(99L);
        });

        assertThat(exception.getMessage()).isEqualTo("User not found with id: 99");
    }

    @Test
    void generateInstructorCoursesReport_should_throwBusinessRuleException_whenUserIsNotAnInstructor() {
        User studentUser = new User("Caio", "caio@alura.com.br", Role.STUDENT, "123456");

        when(userRepository.findById(1L)).thenReturn(Optional.of(studentUser));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            reportService.generateInstructorCoursesReport(1L);
        });

        assertThat(exception.getMessage()).isEqualTo("User with id 1 is not an instructor.");
    }
}