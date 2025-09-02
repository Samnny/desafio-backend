package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.infra.exception.BusinessRuleException;
import br.com.alura.AluraFake.task.TaskRepository;
import br.com.alura.AluraFake.task.model.MultipleChoiceTask;
import br.com.alura.AluraFake.task.model.OpenTextTask;
import br.com.alura.AluraFake.task.model.SingleChoiceTask;
import br.com.alura.AluraFake.task.model.Type;
import br.com.alura.AluraFake.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseService courseService;

    private Course course;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setId(1L);
        course.setStatus(Status.BUILDING);
    }

    @Test
    void publishCourse_should_publish_course_successfully_when_all_rules_are_met() {
        OpenTextTask task1 = new OpenTextTask();
        task1.setOrder(1);
        SingleChoiceTask task2 = new SingleChoiceTask();
        task2.setOrder(2);
        MultipleChoiceTask task3 = new MultipleChoiceTask();
        task3.setOrder(3);

        course.setTasks(new ArrayList<>(List.of(task1, task2, task3)));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseAndTaskType(course, Type.OPEN_TEXT)).thenReturn(1L);
        when(taskRepository.countByCourseAndTaskType(course, Type.SINGLE_CHOICE)).thenReturn(1L);
        when(taskRepository.countByCourseAndTaskType(course, Type.MULTIPLE_CHOICE)).thenReturn(1L);

        courseService.publishCourse(1L);

        assertThat(course.getStatus()).isEqualTo(Status.PUBLISHED);
        assertThat(course.getPublishedAt()).isNotNull();
    }

    @Test
    void publishCourse_should_throw_exception_if_course_is_not_in_building_status() {
        course.setStatus(Status.PUBLISHED);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            courseService.publishCourse(1L);
        });

        assertThat(exception.getMessage()).isEqualTo("Course can only be published if its status is BUILDING.");
    }

    @Test
    void publishCourse_should_throw_exception_if_course_is_missing_a_task_type() {

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        when(taskRepository.countByCourseAndTaskType(course, Type.OPEN_TEXT)).thenReturn(1L);
        when(taskRepository.countByCourseAndTaskType(course, Type.SINGLE_CHOICE)).thenReturn(1L);
        when(taskRepository.countByCourseAndTaskType(course, Type.MULTIPLE_CHOICE)).thenReturn(0L);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            courseService.publishCourse(1L);
        });

        assertThat(exception.getMessage()).isEqualTo("Course must have at least one of each task type to be published.");
    }

    @Test
    void publishCourse_should_throw_exception_if_task_order_is_not_continuous() {
        OpenTextTask task1 = new OpenTextTask();
        task1.setOrder(1);
        MultipleChoiceTask task3 = new MultipleChoiceTask();
        task3.setOrder(3);

        course.setTasks(new ArrayList<>(List.of(task1, task3)));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(taskRepository.countByCourseAndTaskType(any(Course.class), any(Type.class))).thenReturn(1L);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            courseService.publishCourse(1L);
        });

        assertThat(exception.getMessage()).isEqualTo("Task order is not continuous. Expected order 2 but was not found.");
    }
}