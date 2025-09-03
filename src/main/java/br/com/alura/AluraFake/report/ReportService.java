package br.com.alura.AluraFake.report;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.infra.exception.BusinessRuleException;
import br.com.alura.AluraFake.report.dto.CourseReportItemDTO;
import br.com.alura.AluraFake.report.dto.InstructorCoursesReportDTO;
import br.com.alura.AluraFake.user.User;
import br.com.alura.AluraFake.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public InstructorCoursesReportDTO generateInstructorCoursesReport(Long instructorId) {
        // 1. REGRA: Valida se o usuário existe, se não, lança exceção que será convertida para 404
        User user = userRepository.findById(instructorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + instructorId));

        // 2. REGRA: Valida se o usuário é um instrutor, se não, lança exceção para 400
        if (!user.isInstructor()) {
            throw new BusinessRuleException("User with id " + instructorId + " is not an instructor.");
        }

        // 3. Busca a lista de cursos criados pelo instrutor
        List<Course> courses = courseRepository.findByInstructor(user);

        // 4. Converte a lista de entidades 'Course' para a lista de DTOs 'CourseReportItemDTO'
        List<CourseReportItemDTO> courseItems = courses.stream()
                .map(CourseReportItemDTO::new)
                .collect(Collectors.toList());

        // 5. Calcula o total de cursos publicados
        long totalPublished = courseItems.stream()
                .filter(c -> c.status() == Status.PUBLISHED)
                .count();

        // 6. Monta e retorna o DTO final do relatório
        return new InstructorCoursesReportDTO(courseItems, totalPublished);
    }
}