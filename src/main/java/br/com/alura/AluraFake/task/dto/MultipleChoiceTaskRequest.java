package br.com.alura.AluraFake.task.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MultipleChoiceTaskRequest(
        @NotNull Long courseId,
        @NotBlank @Size(min = 4, max = 255) String statement,
        @NotNull @Positive Integer order,
        @NotNull @Size(min = 3, max = 5) List<@Valid OptionRequest> options
) {}
