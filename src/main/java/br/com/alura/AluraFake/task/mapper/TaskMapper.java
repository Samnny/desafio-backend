package br.com.alura.AluraFake.task.mapper;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.task.dto.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.OptionRequest;
import br.com.alura.AluraFake.task.dto.SingleChoiceTaskRequest;
import br.com.alura.AluraFake.task.model.MultipleChoiceTask;
import br.com.alura.AluraFake.task.model.OpenTextTask;
import br.com.alura.AluraFake.task.model.Option;
import br.com.alura.AluraFake.task.model.SingleChoiceTask;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    public OpenTextTask toEntity(OpenTextTaskRequest dto, Course course) {
        OpenTextTask task = new OpenTextTask();
        task.setStatement(dto.statement());
        task.setOrder(dto.order());
        task.setCourse(course);
        return task;
    }

    public SingleChoiceTask toEntity(SingleChoiceTaskRequest dto, Course course) {
        SingleChoiceTask task = new SingleChoiceTask();
        task.setStatement(dto.statement());
        task.setOrder(dto.order());
        task.setCourse(course);
        dto.options().forEach(optDto -> task.addOption(toEntity(optDto)));
        return task;
    }

    public MultipleChoiceTask toEntity(MultipleChoiceTaskRequest dto, Course course) {
        MultipleChoiceTask task = new MultipleChoiceTask();
        task.setStatement(dto.statement());
        task.setOrder(dto.order());
        task.setCourse(course);
        dto.options().forEach(optDto -> task.addOption(toEntity(optDto)));
        return task;
    }

    private Option toEntity(OptionRequest dto) {
        Option option = new Option();
        option.setText(dto.option());
        option.setIsCorrect(dto.isCorrect());
        return option;
    }
}
