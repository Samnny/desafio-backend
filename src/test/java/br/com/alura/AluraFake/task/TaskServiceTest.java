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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Course course;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setId(1L);
        course.setStatus(Status.BUILDING);
    }

    @Test
    void createOpenTextTask_should_create_task_successfully() {
        OpenTextTaskRequest request = new OpenTextTaskRequest(1L, "Enunciado da questão", 1);
        OpenTextTask taskEntity = new OpenTextTask();
        taskEntity.setCourse(course);
        taskEntity.setOrder(request.order());

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskMapper.toEntity(request, course)).thenReturn(taskEntity);
        when(taskRepository.findMaxOrderByCourse(course)).thenReturn(0);
        when(taskRepository.findByCourseAndOrderGreaterThanEqualOrderByOrderAsc(course, 1)).thenReturn(Collections.emptyList());

        taskService.createOpenTextTask(request);

        org.mockito.Mockito.verify(taskRepository).save(any(OpenTextTask.class));
    }

    @Test
    void createOpenTextTask_should_throw_exception_when_course_is_not_building() {
        course.setStatus(Status.PUBLISHED);
        OpenTextTaskRequest request = new OpenTextTaskRequest(1L, "Enunciado", 1);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            taskService.createOpenTextTask(request);
        });

        assertEquals("Cannot add tasks to a course that is not in BUILDING status.", exception.getMessage());
    }

    @Test
    void createOpenTextTask_should_reorder_tasks_when_inserting_in_the_middle() {
        OpenTextTaskRequest newRequest = new OpenTextTaskRequest(1L, "Nova Tarefa", 2);
        OpenTextTask newTaskEntity = new OpenTextTask();
        newTaskEntity.setOrder(2);
        newTaskEntity.setCourse(course);

        OpenTextTask existingTask2 = new OpenTextTask();
        existingTask2.setOrder(2);
        OpenTextTask existingTask3 = new OpenTextTask();
        existingTask3.setOrder(3);
        List<Task> tasksToShift = new ArrayList<>(List.of(existingTask2, existingTask3));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskMapper.toEntity(newRequest, course)).thenReturn(newTaskEntity);
        when(taskRepository.findMaxOrderByCourse(course)).thenReturn(3);
        when(taskRepository.findByCourseAndOrderGreaterThanEqualOrderByOrderAsc(course, 2)).thenReturn(tasksToShift);

        taskService.createOpenTextTask(newRequest);

        ArgumentCaptor<List<Task>> captor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(taskRepository).saveAll(captor.capture());

        List<Task> shiftedTasks = captor.getValue();

        assertEquals(2, shiftedTasks.size());
        assertEquals(3, shiftedTasks.get(0).getOrder()); // Tarefa que era 2 virou 3
        assertEquals(4, shiftedTasks.get(1).getOrder()); // Tarefa que era 3 virou 4

        org.mockito.Mockito.verify(taskRepository).save(newTaskEntity);
    }

    @Test
    void createOpenTextTask_should_throw_exception_for_duplicate_statement() {
        OpenTextTaskRequest request = new OpenTextTaskRequest(1L, "Enunciado duplicado", 1);
        OpenTextTask taskEntity = new OpenTextTask();

        taskEntity.setCourse(course);
        taskEntity.setStatement(request.statement());

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskMapper.toEntity(request, course)).thenReturn(taskEntity); // Agora ele retorna o objeto completo
        when(taskRepository.existsByCourseAndStatement(course, "Enunciado duplicado")).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            taskService.createOpenTextTask(request);
        });
        assertEquals("A task with the same statement already exists in this course.", exception.getMessage());
    }

    @Test
    void createOpenTextTask_should_throw_exception_for_non_sequential_order() {
        OpenTextTaskRequest request = new OpenTextTaskRequest(1L, "Enunciado qualquer", 4);
        OpenTextTask taskEntity = new OpenTextTask();
        taskEntity.setCourse(course);
        taskEntity.setOrder(request.order());

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskMapper.toEntity(request, course)).thenReturn(taskEntity);
        when(taskRepository.findMaxOrderByCourse(course)).thenReturn(2);


        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            taskService.createOpenTextTask(request);
        });

        assertEquals("Invalid task order. The next order should be 3.", exception.getMessage());
    }

    @Test
    void createSingleChoiceTask_should_throw_exception_when_no_option_is_correct() {
        List<OptionRequest> options = List.of(
                new OptionRequest("Opção A", false),
                new OptionRequest("Opção B", false)
        );

        SingleChoiceTaskRequest request = new SingleChoiceTaskRequest(1L, "Enunciado da questão", 1, options);

        SingleChoiceTask taskEntity = new SingleChoiceTask();
        taskEntity.setCourse(course);
        taskEntity.setOrder(request.order());
        taskEntity.setStatement(request.statement());


        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskMapper.toEntity(request, course)).thenReturn(taskEntity);


        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            taskService.createSingleChoiceTask(request);
        });

        assertEquals("Single choice tasks must have exactly one correct option.", exception.getMessage());
    }

    @Test
    void createSingleChoiceTask_should_throw_exception_when_multiple_options_are_correct() {
        List<OptionRequest> options = List.of(
                new OptionRequest("Opção A", true),
                new OptionRequest("Opção B", true),
                new OptionRequest("Opção C", false)
        );

        SingleChoiceTaskRequest request = new SingleChoiceTaskRequest(1L, "Enunciado da questão", 1, options);

        SingleChoiceTask taskEntity = new SingleChoiceTask();
        taskEntity.setCourse(course);
        taskEntity.setOrder(request.order());
        taskEntity.setStatement(request.statement());

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskMapper.toEntity(request, course)).thenReturn(taskEntity);


        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            taskService.createSingleChoiceTask(request);
        });

        assertEquals("Single choice tasks must have exactly one correct option.", exception.getMessage());
    }


    @Test
    void createMultipleChoiceTask_should_throw_exception_when_less_than_two_options_are_correct() {
        List<OptionRequest> options = List.of(
                new OptionRequest("Opção A Correta", true),
                new OptionRequest("Opção B Incorreta", false),
                new OptionRequest("Opção C Incorreta", false)
        );

        MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest(1L, "Enunciado da questão", 1, options);

        MultipleChoiceTask taskEntity = new MultipleChoiceTask();
        taskEntity.setCourse(course);
        taskEntity.setOrder(request.order());
        taskEntity.setStatement(request.statement());

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskMapper.toEntity(request, course)).thenReturn(taskEntity);


        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            taskService.createMultipleChoiceTask(request);
        });

        assertEquals("Multiple choice tasks must have at least two correct options.", exception.getMessage());
    }

    @Test
    void createMultipleChoiceTask_should_throw_exception_when_all_options_are_correct() {
        List<OptionRequest> options = List.of(
                new OptionRequest("Opção A Correta", true),
                new OptionRequest("Opção B Correta", true),
                new OptionRequest("Opção C Correta", true)
        );

        MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest(1L, "Enunciado com tudo certo", 1, options);

        MultipleChoiceTask taskEntity = new MultipleChoiceTask();
        taskEntity.setCourse(course);
        taskEntity.setOrder(request.order());
        taskEntity.setStatement(request.statement());

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskMapper.toEntity(request, course)).thenReturn(taskEntity);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            taskService.createMultipleChoiceTask(request);
        });

        assertEquals("Multiple choice tasks must have at least one incorrect option.", exception.getMessage());
    }
}