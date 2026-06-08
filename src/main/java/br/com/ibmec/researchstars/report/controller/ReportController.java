package br.com.ibmec.researchstars.report.controller;

import br.com.ibmec.researchstars.report.dto.CourseComplianceDto;
import br.com.ibmec.researchstars.report.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/course-compliance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseComplianceDto>> getCourseCompliance() {
        List<CourseComplianceDto> compliance = reportService.getCourseCompliance();
        return ResponseEntity.ok(compliance);
    }
}
