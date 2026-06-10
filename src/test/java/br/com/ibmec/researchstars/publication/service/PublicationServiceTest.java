package br.com.ibmec.researchstars.publication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.ibmec.researchstars.professor.Professor;
import br.com.ibmec.researchstars.professor.ProfessorRepository;
import br.com.ibmec.researchstars.publication.Publication;
import br.com.ibmec.researchstars.publication.PublicationStatus;
import br.com.ibmec.researchstars.publication.PublicationType;
import br.com.ibmec.researchstars.publication.dto.CreatePublicationRequest;
import br.com.ibmec.researchstars.publication.dto.PublicationRequest;
import br.com.ibmec.researchstars.publication.mapper.PublicationMapper;
import br.com.ibmec.researchstars.publication.repository.PublicationRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PublicationServiceTest {

    @Mock
    private PublicationRepository publicationRepository;

    @Mock
    private ProfessorRepository professorRepository;

    private PublicationService publicationService;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-06-10T20:30:00Z"), ZoneId.of("America/Sao_Paulo"));
        publicationService = new PublicationService(publicationRepository, professorRepository, new PublicationMapper(), clock);
    }

    @Test
    void findByIdAllowsAdminToReadAnyPublication() {
        var publication = publication(1L, 10L);
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(publication));

        var result = publicationService.findById(1L, null, true);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.professorId()).isEqualTo(10L);
    }

    @Test
    void findByIdAllowsProfessorToReadOwnPublication() {
        var publication = publication(1L, 10L);
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(publication));

        var result = publicationService.findById(1L, 10L, false);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.professorId()).isEqualTo(10L);
    }

    @Test
    void findByIdRejectsProfessorReadingAnotherProfessorPublication() {
        var publication = publication(1L, 10L);
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(publication));

        assertThatThrownBy(() -> publicationService.findById(1L, 99L, false))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void findByIdIncludesProfessorNameForReview() {
        var publication = publication(1L, 10L);
        var professor = new Professor();
        professor.setName("Thiago Souza");
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(publication));
        when(professorRepository.findById(10L)).thenReturn(Optional.of(professor));

        var result = publicationService.findById(1L, null, true);

        assertThat(result.professorName()).isEqualTo("Thiago Souza");
    }

    @Test
    void createStoresPublicationTypeAndAbntReference() {
        var request = new CreatePublicationRequest(
            "Clean Architecture in Practice",
            "https://example.com/paper",
            LocalDate.of(2026, 1, 10),
            PublicationType.JOURNAL_ARTICLE,
            "LOVELACE, Ada. Clean Architecture in Practice. Journal, 2026."
        );
        when(publicationRepository.save(any(Publication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = publicationService.create(10L, request);

        assertThat(result.publicationType()).isEqualTo(PublicationType.JOURNAL_ARTICLE);
        assertThat(result.abntReference()).isEqualTo("LOVELACE, Ada. Clean Architecture in Practice. Journal, 2026.");
    }

    @Test
    void updateStoresPublicationTypeAndAbntReference() {
        var publication = publication(1L, 10L);
        var request = new PublicationRequest(
            "Updated title",
            "https://example.com/updated",
            LocalDate.of(2025, 12, 1),
            PublicationType.BOOK_CHAPTER,
            "LOVELACE, Ada. Updated chapter. Book, 2025."
        );
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(publication));
        when(publicationRepository.save(publication)).thenReturn(publication);

        var result = publicationService.update(1L, 10L, false, request);

        assertThat(result.publicationType()).isEqualTo(PublicationType.BOOK_CHAPTER);
        assertThat(result.abntReference()).isEqualTo("LOVELACE, Ada. Updated chapter. Book, 2025.");
    }

    @Test
    void updateResetsValidatedPublicationToPending() {
        var publication = publication(1L, 10L);
        publication.setStatus(PublicationStatus.VALIDATED);
        publication.setValidatedByUserId(99L);
        publication.setValidatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        var request = new PublicationRequest(
            "Updated title",
            "https://example.com/updated",
            LocalDate.of(2025, 12, 1),
            PublicationType.BOOK,
            "LOVELACE, Ada. Updated book. Publisher, 2025."
        );
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(publication));
        when(publicationRepository.save(publication)).thenReturn(publication);

        var result = publicationService.update(1L, 10L, false, request);

        assertThat(result.status()).isEqualTo(PublicationStatus.PENDING);
        assertThat(result.validatedByUserId()).isNull();
        assertThat(result.validatedAt()).isNull();
    }

    @Test
    void validateUsesSaoPauloLocalTimeFromClock() {
        var publication = publication(1L, 10L);
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(publication));
        when(publicationRepository.save(publication)).thenReturn(publication);

        var result = publicationService.validate(1L, 99L);

        assertThat(result.validatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 10, 17, 30));
    }

    @Test
    void rejectUsesSaoPauloLocalTimeFromClock() {
        var publication = publication(1L, 10L);
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(publication));
        when(publicationRepository.save(publication)).thenReturn(publication);

        var result = publicationService.reject(1L, 99L);

        assertThat(result.validatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 10, 17, 30));
    }

    private Publication publication(Long id, Long professorId) {
        var publication = new Publication();
        setField(publication, "id", id);
        publication.setProfessorId(professorId);
        publication.setTitle("Original title");
        publication.setLink("https://example.com/original");
        publication.setPublicationDate(LocalDate.of(2024, 5, 1));
        publication.setPublicationType(PublicationType.JOURNAL_ARTICLE);
        publication.setAbntReference("LOVELACE, Ada. Original article. Journal, 2024.");
        publication.setStatus(PublicationStatus.PENDING);
        return publication;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
