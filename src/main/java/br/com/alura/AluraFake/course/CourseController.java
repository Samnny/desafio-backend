package br.com.alura.AluraFake.course;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/new")
    public ResponseEntity<Void> createCourse(@Valid @RequestBody NewCourseDTO newCourse) {
        courseService.createCourse(newCourse);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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