package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.user.Role;
import br.com.alura.AluraFake.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CourseService courseService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "INSTRUCTOR") // Simula um instrutor logado
    void newCourseDTO__should_return_bad_request_when_service_throws_exception() throws Exception {
        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java");
        newCourseDTO.setDescription("Curso de Java");
        newCourseDTO.setEmailInstructor("paulo@alura.com.br");

        when(courseService.createCourse(any(NewCourseDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não é um instrutor"));

        mockMvc.perform(post("/course/new")
                        .with(csrf()) // Adiciona o token CSRF necessário para o POST
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR") // Simula um instrutor logado
    void newCourseDTO__should_return_created_when_new_course_request_is_valid() throws Exception {
        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java");
        newCourseDTO.setDescription("Curso de Java");
        newCourseDTO.setEmailInstructor("paulo@alura.com.br");

        when(courseService.createCourse(any(NewCourseDTO.class))).thenReturn(new Course());

        mockMvc.perform(post("/course/new")
                        .with(csrf()) // Adiciona o token CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser // Simula qualquer usuário logado (pode ser STUDENT ou INSTRUCTOR)
    void listAllCourses__should_list_all_courses() throws Exception {
        User paulo = new User("Paulo", "paulo@alua.com.br", Role.INSTRUCTOR, "123");
        Course java = new Course("Java", "Curso de java", paulo);
        CourseListItemDTO javaDto = new CourseListItemDTO(java);

        when(courseService.findAllCourses()).thenReturn(List.of(javaDto));

        mockMvc.perform(get("/course/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java"))
                .andExpect(jsonPath("$[0].description").value("Curso de java"));
    }
}