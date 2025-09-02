package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.course.dto.CourseListItemDTO;
import br.com.alura.AluraFake.course.dto.CourseResponseDTO; // Importe o DTO de resposta
import br.com.alura.AluraFake.course.dto.NewCourseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder; // Importe o UriComponentsBuilder

import java.net.URI; // Importe a classe URI
import java.util.List;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/new")
    public ResponseEntity<CourseResponseDTO> createCourse(@Valid @RequestBody NewCourseDTO newCourse, UriComponentsBuilder uriBuilder) {
        Course courseSalvo = courseService.createCourse(newCourse);
        URI uri = uriBuilder.path("/course/{id}").buildAndExpand(courseSalvo.getId()).toUri();
        return ResponseEntity.created(uri).body(new CourseResponseDTO(courseSalvo));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CourseListItemDTO>> getAllCourses() {
        List<CourseListItemDTO> courses = courseService.findAllCourses();
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publishCourse(@PathVariable Long id) {
        courseService.publishCourse(id);
        return ResponseEntity.ok().build();
    }
}