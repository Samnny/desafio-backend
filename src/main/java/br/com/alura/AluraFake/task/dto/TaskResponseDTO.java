package br.com.alura.AluraFake.task.dto;

import br.com.alura.AluraFake.task.model.Task;

public record TaskResponseDTO(
        Long id,
        String statement,
        Integer order,
        String taskType
) {
    public TaskResponseDTO(Task task) {
        this(
                task.getId(),
                task.getStatement(),
                task.getOrder(),
                task.getTaskType()
        );
    }
}