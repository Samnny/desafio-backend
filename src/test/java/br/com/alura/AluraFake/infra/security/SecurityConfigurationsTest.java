package br.com.alura.AluraFake.infra.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigurationsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/course/all"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/course/new"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void shouldReturn403WhenStudentAccessesInstructorEndpoint() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/course/new")
                        .contentType("application/json")
                        .content("""
                        {
                            "title": "Curso Proibido",
                            "description": "Tentativa de criacao",
                            "emailInstructor": "paulo@alura.com.br"
                        }
                        """))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void shouldReturn200WhenInstructorAccessesPublicEndpoint() throws Exception {
        mockMvc.perform(get("/course/all"))
                .andExpect(status().isOk());
    }
}