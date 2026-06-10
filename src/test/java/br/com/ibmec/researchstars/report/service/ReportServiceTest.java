package br.com.ibmec.researchstars.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.ibmec.researchstars.course.Course;
import br.com.ibmec.researchstars.course.CourseRepository;
import br.com.ibmec.researchstars.professor.Professor;
import br.com.ibmec.researchstars.professor.ProfessorRepository;
import br.com.ibmec.researchstars.publication.repository.PublicationRepository;
import br.com.ibmec.researchstars.report.dto.CourseComplianceDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private PublicationRepository publicationRepository;

    @Mock
    private ReportingWindowService reportingWindowService;

    private ReportService reportService;

    private Course courseA;
    private Professor profA;
    private Professor profB;
    private ReportingWindow reportingWindow;

    @BeforeEach
    void setUp() {
        reportingWindow = new ReportingWindow(LocalDate.of(2023, 1, 1), LocalDate.of(2026, 6, 10));
        when(reportingWindowService.defaultWindow()).thenReturn(reportingWindow);
        reportService = new ReportService(courseRepository, professorRepository, publicationRepository, reportingWindowService);

        courseA = new Course("Computacao", "COMP");
        ReflectionTestUtils.setField(courseA, "id", 10L);

        profA = new Professor();
        ReflectionTestUtils.setField(profA, "id", 100L);
        profA.setName("Alice");
        profA.setStatus(Professor.Status.APPROVED);
        profA.setCourseIds(Set.of(10L));

        profB = new Professor();
        ReflectionTestUtils.setField(profB, "id", 200L);
        profB.setName("Bob");
        profB.setStatus(Professor.Status.APPROVED);
        profB.setCourseIds(Set.of(10L));
    }

    @Test
    void test_deve_retornar_conformidade_do_curso_com_porcentagem_correta() {
        // Arrange
        when(courseRepository.findAll()).thenReturn(List.of(courseA));
        when(professorRepository.findAll()).thenReturn(List.of(profA));
        when(publicationRepository.countValidatedBetween(eq(100L), eq(reportingWindow.startDate()), eq(reportingWindow.endDate()))).thenReturn(10L); // >= 9

        // Act
        List<CourseComplianceDto> reports = reportService.getCourseCompliance();

        // Assert
        assertThat(reports).hasSize(1);
        CourseComplianceDto compliance = reports.get(0);
        assertThat(compliance.getCourseCode()).isEqualTo("COMP");
        assertThat(compliance.getTotalApprovedProfessors()).isEqualTo(1L);
        assertThat(compliance.getCompliantProfessors()).isEqualTo(1L);
        assertThat(compliance.getTotalCompliantProfessors()).isEqualTo(1L);
        assertThat(compliance.getCompliancePercentage()).isEqualTo(100.0);
    }

    @Test
    void test_deve_retornar_zero_porcento_quando_professor_nao_atinge_meta() {
        // Arrange
        when(courseRepository.findAll()).thenReturn(List.of(courseA));
        when(professorRepository.findAll()).thenReturn(List.of(profA));
        when(publicationRepository.countValidatedBetween(eq(100L), eq(reportingWindow.startDate()), eq(reportingWindow.endDate()))).thenReturn(8L); // < 9

        // Act
        List<CourseComplianceDto> reports = reportService.getCourseCompliance();

        // Assert
        assertThat(reports).hasSize(1);
        CourseComplianceDto compliance = reports.get(0);
        assertThat(compliance.getTotalApprovedProfessors()).isEqualTo(1L);
        assertThat(compliance.getCompliantProfessors()).isEqualTo(0L);
        assertThat(compliance.getTotalCompliantProfessors()).isEqualTo(0L);
        assertThat(compliance.getCompliancePercentage()).isEqualTo(0.0);
    }

    @Test
    void getCourseComplianceCountsOnlyApprovedProfessors() {
        profB.setStatus(Professor.Status.PENDING);
        when(courseRepository.findAll()).thenReturn(List.of(courseA));
        when(professorRepository.findAll()).thenReturn(List.of(profA, profB));
        when(publicationRepository.countValidatedBetween(eq(100L), eq(reportingWindow.startDate()), eq(reportingWindow.endDate()))).thenReturn(9L);

        CourseComplianceDto compliance = reportService.getCourseCompliance().get(0);

        assertThat(compliance.getTotalApprovedProfessors()).isEqualTo(1L);
        assertThat(compliance.getCompliantProfessors()).isEqualTo(1L);
        assertThat(compliance.getProfessorCompliance())
                .extracting("professorId")
                .containsExactly(100L);
        verify(publicationRepository, never()).countValidatedBetween(eq(200L), eq(reportingWindow.startDate()), eq(reportingWindow.endDate()));
    }

    @Test
    void getCourseComplianceCalculatesPercentageFromCompliantAndApprovedProfessors() {
        when(courseRepository.findAll()).thenReturn(List.of(courseA));
        when(professorRepository.findAll()).thenReturn(List.of(profA, profB));
        when(publicationRepository.countValidatedBetween(eq(100L), eq(reportingWindow.startDate()), eq(reportingWindow.endDate()))).thenReturn(9L);
        when(publicationRepository.countValidatedBetween(eq(200L), eq(reportingWindow.startDate()), eq(reportingWindow.endDate()))).thenReturn(8L);

        CourseComplianceDto compliance = reportService.getCourseCompliance().get(0);

        assertThat(compliance.getTotalApprovedProfessors()).isEqualTo(2L);
        assertThat(compliance.getCompliantProfessors()).isEqualTo(1L);
        assertThat(compliance.getCompliancePercentage()).isEqualTo(50.0);
    }

    @Test
    void getCourseComplianceIncludesApprovedProfessorPublicationCounts() {
        when(courseRepository.findAll()).thenReturn(List.of(courseA));
        when(professorRepository.findAll()).thenReturn(List.of(profA, profB));
        when(publicationRepository.countValidatedBetween(eq(100L), eq(reportingWindow.startDate()), eq(reportingWindow.endDate()))).thenReturn(3L);
        when(publicationRepository.countValidatedBetween(eq(200L), eq(reportingWindow.startDate()), eq(reportingWindow.endDate()))).thenReturn(9L);

        CourseComplianceDto compliance = reportService.getCourseCompliance().get(0);

        assertThat(compliance.getProfessorCompliance()).hasSize(2);
        assertThat(compliance.getProfessorCompliance().get(0).getProfessorId()).isEqualTo(100L);
        assertThat(compliance.getProfessorCompliance().get(0).getProfessorName()).isEqualTo("Alice");
        assertThat(compliance.getProfessorCompliance().get(0).getValidatedPublications()).isEqualTo(3L);
        assertThat(compliance.getProfessorCompliance().get(0).isCompliant()).isFalse();
        assertThat(compliance.getProfessorCompliance().get(1).getProfessorId()).isEqualTo(200L);
        assertThat(compliance.getProfessorCompliance().get(1).getProfessorName()).isEqualTo("Bob");
        assertThat(compliance.getProfessorCompliance().get(1).getValidatedPublications()).isEqualTo(9L);
        assertThat(compliance.getProfessorCompliance().get(1).isCompliant()).isTrue();
    }

    @Test
    void getCourseComplianceCountsMultiCourseProfessorInEachApprovedCourse() {
        var courseB = new Course("Data Science", "DS");
        ReflectionTestUtils.setField(courseB, "id", 20L);
        profA.setCourseIds(Set.of(10L, 20L));
        when(courseRepository.findAll()).thenReturn(List.of(courseA, courseB));
        when(professorRepository.findAll()).thenReturn(List.of(profA));
        when(publicationRepository.countValidatedBetween(eq(100L), eq(reportingWindow.startDate()), eq(reportingWindow.endDate())))
                .thenReturn(9L);

        List<CourseComplianceDto> reports = reportService.getCourseCompliance();

        assertThat(reports).hasSize(2);
        assertThat(reports)
                .extracting(CourseComplianceDto::getCourseCode)
                .containsExactly("COMP", "DS");
        assertThat(reports)
                .extracting(CourseComplianceDto::getTotalApprovedProfessors)
                .containsExactly(1L, 1L);
        assertThat(reports)
                .extracting(CourseComplianceDto::getCompliantProfessors)
                .containsExactly(1L, 1L);
    }
}
