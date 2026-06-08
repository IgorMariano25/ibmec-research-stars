package br.com.ibmec.researchstars.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.mockito.InjectMocks;
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

    @InjectMocks
    private ReportService reportService;

    private Course courseA;
    private Professor profA;

    @BeforeEach
    void setUp() {
        courseA = new Course("Computacao", "COMP");
        ReflectionTestUtils.setField(courseA, "id", 10L);

        profA = new Professor();
        ReflectionTestUtils.setField(profA, "id", 100L);
        profA.setName("Alice");
        profA.setStatus(Professor.Status.APPROVED);
        profA.setCourseIds(Set.of(10L));
    }

    @Test
    void test_deve_retornar_conformidade_do_curso_com_porcentagem_correta() {
        // Arrange
        when(courseRepository.findAll()).thenReturn(List.of(courseA));
        when(professorRepository.findAll()).thenReturn(List.of(profA));
        when(publicationRepository.countValidatedSince(eq(100L), any(LocalDate.class))).thenReturn(10L); // >= 9

        // Act
        List<CourseComplianceDto> reports = reportService.getCourseCompliance();

        // Assert
        assertThat(reports).hasSize(1);
        CourseComplianceDto compliance = reports.get(0);
        assertThat(compliance.getCourseCode()).isEqualTo("COMP");
        assertThat(compliance.getTotalApprovedProfessors()).isEqualTo(1L);
        assertThat(compliance.getTotalCompliantProfessors()).isEqualTo(1L);
        assertThat(compliance.getCompliancePercentage()).isEqualTo(100.0);
    }

    @Test
    void test_deve_retornar_zero_porcento_quando_professor_nao_atinge_meta() {
        // Arrange
        when(courseRepository.findAll()).thenReturn(List.of(courseA));
        when(professorRepository.findAll()).thenReturn(List.of(profA));
        when(publicationRepository.countValidatedSince(eq(100L), any(LocalDate.class))).thenReturn(8L); // < 9

        // Act
        List<CourseComplianceDto> reports = reportService.getCourseCompliance();

        // Assert
        assertThat(reports).hasSize(1);
        CourseComplianceDto compliance = reports.get(0);
        assertThat(compliance.getTotalApprovedProfessors()).isEqualTo(1L);
        assertThat(compliance.getTotalCompliantProfessors()).isEqualTo(0L);
        assertThat(compliance.getCompliancePercentage()).isEqualTo(0.0);
    }
}
