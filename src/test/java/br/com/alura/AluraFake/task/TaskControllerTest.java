package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.task.dto.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.OptionRequest;
import br.com.alura.AluraFake.task.dto.SingleChoiceTaskRequest;
import br.com.alura.AluraFake.task.model.OpenTextTask;
import br.com.alura.AluraFake.task.model.SingleChoiceTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar 201 Created e o DTO da tarefa ao criar OpenTextTask com sucesso")
    @WithMockUser(roles = "INSTRUCTOR")
    void createOpenTextTask_shouldReturnCreated_whenRequestIsValid() throws Exception {
        OpenTextTaskRequest request = new OpenTextTaskRequest(1L, "Enunciado válido", 1);
        OpenTextTask taskSalva = new OpenTextTask();
        taskSalva.setId(10L); // Simulamos que o banco gerou um ID
        taskSalva.setStatement(request.statement());
        taskSalva.setOrder(request.order());

        when(taskService.createOpenTextTask(any(OpenTextTaskRequest.class))).thenReturn(taskSalva);

        mockMvc.perform(post("/task/new/opentext")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(10L));
    }


    @Test
    @DisplayName("Deve retornar 201 Created e o DTO da tarefa ao criar SingleChoiceTask com sucesso")
    @WithMockUser(roles = "INSTRUCTOR")
    void createSingleChoiceTask_shouldReturnCreated_whenRequestIsValid() throws Exception {
        List<OptionRequest> options = List.of(
                new OptionRequest("Opção A", true),
                new OptionRequest("Opção B", false)
        );
        SingleChoiceTaskRequest request = new SingleChoiceTaskRequest(1L, "Enunciado de única escolha", 2, options);

        SingleChoiceTask taskSalva = new SingleChoiceTask();
        taskSalva.setId(11L); // Um novo ID simulado
        taskSalva.setStatement(request.statement());
        taskSalva.setOrder(request.order());

        when(taskService.createSingleChoiceTask(any(SingleChoiceTaskRequest.class))).thenReturn(taskSalva);

        mockMvc.perform(post("/task/new/singlechoice")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(11L))
                .andExpect(jsonPath("$.statement").value("Enunciado de única escolha"));
    }
}