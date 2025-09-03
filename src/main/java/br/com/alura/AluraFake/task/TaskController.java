package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.task.dto.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.SingleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.TaskResponseDTO;
import br.com.alura.AluraFake.task.model.Task;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/task/new")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/opentext")
    public ResponseEntity<TaskResponseDTO> newOpenTextExercise(@RequestBody @Valid OpenTextTaskRequest request, UriComponentsBuilder uriBuilder) {
        Task task = taskService.createOpenTextTask(request);
        URI uri = uriBuilder.path("/task/{id}").buildAndExpand(task.getId()).toUri();
        return ResponseEntity.created(uri).body(new TaskResponseDTO(task));
    }

    @PostMapping("/singlechoice")
    public ResponseEntity<TaskResponseDTO> newSingleChoice(@RequestBody @Valid SingleChoiceTaskRequest request, UriComponentsBuilder uriBuilder) {
        Task task = taskService.createSingleChoiceTask(request);
        URI uri = uriBuilder.path("/task/{id}").buildAndExpand(task.getId()).toUri();
        return ResponseEntity.created(uri).body(new TaskResponseDTO(task));
    }

    @PostMapping("/multiplechoice")
    public ResponseEntity<TaskResponseDTO> newMultipleChoice(@RequestBody @Valid MultipleChoiceTaskRequest request, UriComponentsBuilder uriBuilder) {
        Task task = taskService.createMultipleChoiceTask(request);
        URI uri = uriBuilder.path("/task/{id}").buildAndExpand(task.getId()).toUri();
        return ResponseEntity.created(uri).body(new TaskResponseDTO(task));
    }
}