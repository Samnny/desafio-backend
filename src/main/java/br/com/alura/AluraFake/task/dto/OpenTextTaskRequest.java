package br.com.alura.AluraFake.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OpenTextTaskRequest(
        @NotNull Long courseId,
        @NotBlank @Size(min = 4, max = 255) String statement,
        @NotNull @Positive Integer order
) {}
