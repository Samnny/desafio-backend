package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.task.dto.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.SingleChoiceTaskRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task/new")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/opentext")
    public ResponseEntity newOpenTextExercise(@RequestBody @Valid OpenTextTaskRequest request) {
        taskService.createOpenTextTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/singlechoice")
    public ResponseEntity newSingleChoice(@RequestBody @Valid SingleChoiceTaskRequest request) {
        taskService.createSingleChoiceTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/multiplechoice")
    public ResponseEntity newMultipleChoice(@RequestBody @Valid MultipleChoiceTaskRequest request) {
        taskService.createMultipleChoiceTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}