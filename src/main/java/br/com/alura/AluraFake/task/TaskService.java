package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.infra.exception.BusinessRuleException;
import br.com.alura.AluraFake.task.dto.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.OptionRequest;
import br.com.alura.AluraFake.task.dto.SingleChoiceTaskRequest;
import br.com.alura.AluraFake.task.mapper.TaskMapper;
import br.com.alura.AluraFake.task.model.MultipleChoiceTask;
import br.com.alura.AluraFake.task.model.OpenTextTask;
import br.com.alura.AluraFake.task.model.SingleChoiceTask;
import br.com.alura.AluraFake.task.model.Task;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CourseRepository courseRepository;
    private final TaskMapper taskMapper;

    @Transactional
    public Task createOpenTextTask(OpenTextTaskRequest request) {
        Course course = findAndValidateCourse(request.courseId());
        OpenTextTask task = taskMapper.toEntity(request, course);

        validateAndPrepareForSave(task);

        return taskRepository.save(task);
    }

    @Transactional
    public Task createSingleChoiceTask(SingleChoiceTaskRequest request) {
        Course course = findAndValidateCourse(request.courseId());
        SingleChoiceTask task = taskMapper.toEntity(request, course);

        validateAndPrepareForSave(task);
        validateSingleChoiceOptions(request.options()); // Validação específica
        validateCommonOptionsRules(request.statement(), request.options()); // Validação comum

        return taskRepository.save(task);
    }

    @Transactional
    public Task createMultipleChoiceTask(MultipleChoiceTaskRequest request) {
        Course course = findAndValidateCourse(request.courseId());
        MultipleChoiceTask task = taskMapper.toEntity(request, course);

        validateAndPrepareForSave(task);
        validateMultipleChoiceOptions(request.options()); // Validação específica
        validateCommonOptionsRules(request.statement(), request.options()); // Validação comum

        return taskRepository.save(task);
    }

    private void validateAndPrepareForSave(Task task) {
        // Regra: Enunciado único por curso
        if (taskRepository.existsByCourseAndStatement(task.getCourse(), task.getStatement())) {
            throw new BusinessRuleException("A task with the same statement already exists in this course.");
        }

        // Regra: Ordem contínua
        int maxOrder = taskRepository.findMaxOrderByCourse(task.getCourse());
        if (task.getOrder() > maxOrder + 1) {
            throw new BusinessRuleException("Invalid task order. The next order should be " + (maxOrder + 1) + ".");
        }

        // Reordena as tasks existentes se necessário
        reorderTasks(task.getCourse(), task.getOrder());
    }

    private Course findAndValidateCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        // Ajuste: Usando o enum Status
        if (course.getStatus() != Status.BUILDING) {
            throw new BusinessRuleException("Cannot add tasks to a course that is not in BUILDING status.");
        }
        return course;
    }

    private void reorderTasks(Course course, Integer startingOrder) {
        List<Task> tasksToShift = taskRepository.findByCourseAndOrderGreaterThanEqualOrderByOrderAsc(course, startingOrder);
        tasksToShift.forEach(t -> t.setOrder(t.getOrder() + 1));
        taskRepository.saveAll(tasksToShift);
    }


    // Implementação da validação para Alternativa Única
    private void validateSingleChoiceOptions(List<OptionRequest> options) {
        long correctOptionsCount = options.stream().filter(OptionRequest::isCorrect).count();
        if (correctOptionsCount != 1) {
            throw new BusinessRuleException("Single choice tasks must have exactly one correct option.");
        }
    }

    // Implementação da validação para Múltipla Escolha
    private void validateMultipleChoiceOptions(List<OptionRequest> options) {
        long correctOptionsCount = options.stream().filter(OptionRequest::isCorrect).count();
        if (correctOptionsCount < 2) {
            throw new BusinessRuleException("Multiple choice tasks must have at least two correct options.");
        }
        if (correctOptionsCount == options.size()) {
            throw new BusinessRuleException("Multiple choice tasks must have at least one incorrect option.");
        }
    }

    // Implementação das validações comuns a ambas
    private void validateCommonOptionsRules(String statement, List<OptionRequest> options) {
        // Pega o texto de todas as alternativas e coloca em um Set
        Set<String> optionTexts = options.stream()
                .map(OptionRequest::option)
                .collect(Collectors.toSet());

        // Regra: Alternativas não podem ser iguais entre si
        // Se o tamanho do Set for diferente do tamanho da Lista, significa que havia itens duplicados
        if (optionTexts.size() != options.size()) {
            throw new BusinessRuleException("Options cannot have duplicate texts.");
        }

        // Regra: Alternativas não podem ser iguais ao enunciado da atividade
        if (optionTexts.contains(statement)) {
            throw new BusinessRuleException("An option's text cannot be the same as the task's statement.");
        }
    }
}