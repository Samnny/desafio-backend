package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.course.dto.CourseListItemDTO;
import br.com.alura.AluraFake.course.dto.NewCourseDTO;
import br.com.alura.AluraFake.infra.exception.BusinessRuleException;
import br.com.alura.AluraFake.user.Role;
import br.com.alura.AluraFake.user.User;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar 201 Created e o DTO do curso quando a requisição for válida")
    @WithMockUser(roles = "INSTRUCTOR")
    void createCourse_shouldReturnCreated_whenRequestIsValid() throws Exception {
        // Arrange
        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java Completo");
        newCourseDTO.setDescription("Do básico ao avançado");
        newCourseDTO.setEmailInstructor("paulo@alura.com.br");

        User instructor = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR, "123456");
        Course courseSalvo = new Course(newCourseDTO.getTitle(), newCourseDTO.getDescription(), instructor);
        courseSalvo.setId(1L);

        when(courseService.createCourse(any(NewCourseDTO.class))).thenReturn(courseSalvo);

        // Act & Assert
        mockMvc.perform(post("/course/new")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/course/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Java Completo"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando o serviço lançar uma exceção de negócio")
    @WithMockUser(roles = "INSTRUCTOR")
    void createCourse_shouldReturnBadRequest_whenServiceThrowsException() throws Exception {
        // Arrange
        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java");
        newCourseDTO.setDescription("Curso de Java");
        newCourseDTO.setEmailInstructor("aluno@email.com");

        when(courseService.createCourse(any(NewCourseDTO.class)))
                .thenThrow(new BusinessRuleException("Usuário não é um instrutor"));

        // Act & Assert
        mockMvc.perform(post("/course/new")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 200 OK e a lista de cursos ao listar todos")
    @WithMockUser
    void listAllCourses_should_list_all_courses() throws Exception {
        // Arrange
        // CORREÇÃO: Usando o construtor correto (name, email, role, password)
        User paulo = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR, "123456");
        Course java = new Course("Java", "Curso de java", paulo);
        CourseListItemDTO javaDto = new CourseListItemDTO(java);

        when(courseService.findAllCourses()).thenReturn(List.of(javaDto));

        // Act & Assert
        mockMvc.perform(get("/course/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java"))
                .andExpect(jsonPath("$.[0].description").value("Curso de java"));
    }
}