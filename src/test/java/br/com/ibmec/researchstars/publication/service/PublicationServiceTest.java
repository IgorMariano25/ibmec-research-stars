package br.com.ibmec.researchstars.publication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.ibmec.researchstars.publication.Publication;
import br.com.ibmec.researchstars.publication.PublicationStatus;
import br.com.ibmec.researchstars.publication.mapper.PublicationMapper;
import br.com.ibmec.researchstars.publication.repository.PublicationRepository;
import java.time.LocalDate;
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

    private PublicationService publicationService;

    @BeforeEach
    void setUp() {
        publicationService = new PublicationService(publicationRepository, new PublicationMapper());
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

    private Publication publication(Long id, Long professorId) {
        var publication = new Publication();
        setField(publication, "id", id);
        publication.setProfessorId(professorId);
        publication.setTitle("Paper");
        publication.setLink("https://example.org/paper");
        publication.setPublicationDate(LocalDate.now());
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
