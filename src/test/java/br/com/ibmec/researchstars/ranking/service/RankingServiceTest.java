package br.com.ibmec.researchstars.ranking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import br.com.ibmec.researchstars.professor.Professor;
import br.com.ibmec.researchstars.professor.ProfessorRepository;
import br.com.ibmec.researchstars.publication.repository.PublicationRepository;
import br.com.ibmec.researchstars.ranking.dto.MyRankingResponseDto;
import br.com.ibmec.researchstars.ranking.dto.RankingEntryDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private PublicationRepository publicationRepository;

    @InjectMocks
    private RankingService rankingService;

    private Professor profA;
    private Professor profB;

    @BeforeEach
    void setUp() {
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
        when(publicationRepository.countValidatedSince(eq(100L), any(LocalDate.class))).thenReturn(5L);
        when(publicationRepository.countValidatedSince(eq(200L), any(LocalDate.class))).thenReturn(10L);

        // Act
        Page<RankingEntryDto> result = rankingService.getRanking(PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Bob");
        assertThat(result.getContent().get(0).getValidatedPublications()).isEqualTo(10L);
        assertThat(result.getContent().get(1).getName()).isEqualTo("Alice");
        assertThat(result.getContent().get(1).getValidatedPublications()).isEqualTo(5L);
    }

    @Test
    void test_deve_retornar_posicao_correta_do_proprio_professor() {
        // Arrange
        when(professorRepository.findAll()).thenReturn(List.of(profA, profB));
        when(professorRepository.findById(100L)).thenReturn(Optional.of(profA));
        when(publicationRepository.countValidatedSince(eq(100L), any(LocalDate.class))).thenReturn(5L);
        when(publicationRepository.countValidatedSince(eq(200L), any(LocalDate.class))).thenReturn(10L);

        // Act
        MyRankingResponseDto myRanking = rankingService.getMyRanking(100L);

        // Assert
        assertThat(myRanking.getPosition()).isEqualTo(2);
        assertThat(myRanking.getValidatedPublications()).isEqualTo(5L);
    }
}
