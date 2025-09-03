package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.course.dto.CourseListItemDTO;
import br.com.alura.AluraFake.course.dto.NewCourseDTO;
import br.com.alura.AluraFake.infra.exception.BusinessRuleException;
import br.com.alura.AluraFake.task.TaskRepository;
import br.com.alura.AluraFake.task.model.Type;
import br.com.alura.AluraFake.task.model.Task;
import br.com.alura.AluraFake.user.User;
import br.com.alura.AluraFake.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public void publishCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        validateCourseStatus(course);
        validateTaskTypes(course);
        validateTaskOrder(course);

        course.setStatus(Status.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
    }

    private void validateCourseStatus(Course course) {
        if (course.getStatus() != Status.BUILDING) {
            throw new BusinessRuleException("Course can only be published if its status is BUILDING.");
        }
    }

    private void validateTaskTypes(Course course) {
        boolean hasOpenText = taskRepository.countByCourseAndTaskType(course, Type.OPEN_TEXT) > 0;
        boolean hasSingleChoice = taskRepository.countByCourseAndTaskType(course, Type.SINGLE_CHOICE) > 0;
        boolean hasMultipleChoice = taskRepository.countByCourseAndTaskType(course, Type.MULTIPLE_CHOICE) > 0;

        if (!hasOpenText || !hasSingleChoice || !hasMultipleChoice) {
            throw new BusinessRuleException("Course must have at least one of each task type to be published.");
        }
    }

    private void validateTaskOrder(Course course) {
        List<Task> tasks = course.getTasks();
        if (tasks.isEmpty()) {
            throw new BusinessRuleException("Course must have at least one task to be published.");
        }

        tasks.sort(Comparator.comparing(Task::getOrder));

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getOrder() != i + 1) {
                throw new BusinessRuleException("Task order is not continuous. Expected order " + (i + 1) + " but was not found.");
            }
        }
    }

    public Course createCourse(NewCourseDTO newCourseDTO) {
        User instructor = userRepository
                .findByEmail(newCourseDTO.getEmailInstructor())
                .filter(User::isInstructor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não é um instrutor"));

        Course course = new Course(newCourseDTO.getTitle(), newCourseDTO.getDescription(), instructor);
        return courseRepository.save(course);
    }

    public List<CourseListItemDTO> findAllCourses() {
        return courseRepository.findAll().stream()
                .map(CourseListItemDTO::new)
                .toList();
    }
}