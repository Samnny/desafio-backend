package br.com.alura.AluraFake.instructor;

import br.com.alura.AluraFake.report.ReportService;
import br.com.alura.AluraFake.report.dto.InstructorCoursesReportDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/instructor")
@RequiredArgsConstructor
public class InstructorController {

    private final ReportService reportService;

    @GetMapping("/{id}/courses")
    public ResponseEntity<InstructorCoursesReportDTO> getCoursesReport(@PathVariable Long id) {
        InstructorCoursesReportDTO report = reportService.generateInstructorCoursesReport(id);
        return ResponseEntity.ok(report);
    }
}