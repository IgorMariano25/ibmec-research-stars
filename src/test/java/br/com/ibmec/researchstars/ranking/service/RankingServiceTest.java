package br.com.ibmec.researchstars.ranking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import br.com.ibmec.researchstars.professor.Professor;
import br.com.ibmec.researchstars.professor.ProfessorRepository;
import br.com.ibmec.researchstars.publication.repository.PublicationRepository;
import br.com.ibmec.researchstars.report.service.ReportingWindow;
import br.com.ibmec.researchstars.report.service.ReportingWindowService;
import br.com.ibmec.researchstars.ranking.dto.MyRankingResponseDto;
import br.com.ibmec.researchstars.ranking.dto.RankingEntryDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private PublicationRepository publicationRepository;

    private RankingService rankingService;

    private Professor profA;
    private Professor profB;
    private ReportingWindow reportingWindow;

    @BeforeEach
    void setUp() {
        var reportingWindowService = org.mockito.Mockito.mock(ReportingWindowService.class);
        reportingWindow = new ReportingWindow(LocalDate.of(2023, 1, 1), LocalDate.of(2026, 6, 10));
        when(reportingWindowService.defaultWindow()).thenReturn(reportingWindow);
        rankingService = new RankingService(professorRepository, publicationRepository, reportingWindowService);

        profA = new Professor();
        ReflectionTestUtils.setField(profA, "id", 100L);
        profA.setName("Alice");
        profA.setStatus(Professor.Status.APPROVED);

        profB = new Professor();
        ReflectionTestUtils.setField(profB, "id", 200L);
        profB.setName("Bob");
        profB.setStatus(Professor.Status.APPROVED);
    }

    @Test
    void test_deve_retornar_ranking_ordenado_pelo_numero_de_publicacoes() {
        // Arrange
        when(professorRepository.findAll()).thenReturn(List.of(profA, profB));
        when(publicationRepository.countValidatedBetween(eq(100L), eq(reportingWindow.startDate()), eq(reportingWindow.endDate()))).thenReturn(5L);
        when(publicationRepository.countValidatedBetween(eq(200L), eq(reportingWindow.startDate()), eq(reportingWindow.endDate()))).thenReturn(10L);

        // Act
        List<RankingEntryDto> result = rankingService.getRanking();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Bob");
        assertThat(result.get(0).getValidatedPublicationsLast3Years()).isEqualTo(10L);
        assertThat(result.get(1).getName()).isEqualTo("Alice");
        assertThat(result.get(1).getValidatedPublicationsLast3Years()).isEqualTo(5L);
    }

    @Test
    void test_deve_retornar_posicao_correta_do_proprio_professor() {
        // Arrange
        when(professorRepository.findAll()).thenReturn(List.of(profA, profB));
        when(professorRepository.findById(100L)).thenReturn(Optional.of(profA));
        when(publicationRepository.countValidatedBetween(eq(100L), eq(reportingWindow.startDate()), eq(reportingWindow.endDate()))).thenReturn(5L);
        when(publicationRepository.countValidatedBetween(eq(200L), eq(reportingWindow.startDate()), eq(reportingWindow.endDate()))).thenReturn(10L);

        // Act
        MyRankingResponseDto myRanking = rankingService.getMyRanking(100L);

        // Assert
        assertThat(myRanking.getRank()).isEqualTo(2);
        assertThat(myRanking.getValidatedPublicationsLast3Years()).isEqualTo(5L);
    }
}
