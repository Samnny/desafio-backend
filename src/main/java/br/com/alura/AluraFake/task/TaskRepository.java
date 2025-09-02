package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    boolean existsByCourseAndStatement(Course course, String statement);

    @Query("SELECT COALESCE(MAX(t.order), 0) FROM Task t WHERE t.course = :course")
    int findMaxOrderByCourse(@Param("course") Course course);

    List<Task> findByCourseAndOrderGreaterThanEqualOrderByOrderAsc(Course course, Integer order);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.course = :course AND t.taskType = :taskTypeString")
    long countByCourseAndTaskType(@Param("course") Course course, @Param("taskTypeString") String taskTypeString);

    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.course = :course AND t.order = :order")
    boolean existsByCourseAndOrder(@Param("course") Course course, @Param("order") int order);
}