package br.com.ibmec.researchstars.professor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.ibmec.researchstars.course.CourseRepository;
import br.com.ibmec.researchstars.professor.dto.ProfessorUpdateRequest;
import br.com.ibmec.researchstars.professor.exception.ProfessorConflictException;
import br.com.ibmec.researchstars.professor.exception.ProfessorIntegrationException;
import br.com.ibmec.researchstars.professor.exception.ProfessorNotFoundException;
import br.com.ibmec.researchstars.professor.exception.ProfessorStateException;
import br.com.ibmec.researchstars.professor.integration.CourseGateway;
import br.com.ibmec.researchstars.professor.integration.CurrentUserProvider;
import br.com.ibmec.researchstars.professor.integration.ProfessorPublicationsGateway;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ProfessorServiceTest {

    @Mock
    private ProfessorRepository repository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private CourseGateway courseGateway;

    @Mock
    private ProfessorPublicationsGateway publicationsGateway;

    @Mock
    private CourseRepository courseRepository;

    private ProfessorService service;

    @BeforeEach
    void setUp() {
        service = new ProfessorService(repository, currentUserProvider, courseGateway, publicationsGateway,
                courseRepository);
    }

    // ── list ──────────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void listReturnsMappedPage() {
        var professor = buildProfessor(1L, Professor.Status.PENDING);
        when(repository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(professor)));

        var result = service.list(Professor.Status.PENDING, "ada", 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).id()).isEqualTo(1L);
        assertThat(result.content().get(0).name()).isEqualTo("Ada Lovelace");
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findByIdReturnsProfessorDetail() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildProfessor(1L, Professor.Status.APPROVED)));

        var result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("ada@ibmec.br");
        assertThat(result.status()).isEqualTo(Professor.Status.APPROVED);
    }

    @Test
    void findByIdThrowsNotFoundWhenProfessorDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ProfessorNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── findMe ────────────────────────────────────────────────────────────────

    @Test
    void findMeReturnsProfessorForCurrentUser() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(repository.findByUserId(10L)).thenReturn(Optional.of(buildProfessor(1L, Professor.Status.APPROVED)));

        var result = service.findMe();

        assertThat(result.userId()).isEqualTo(10L);
    }

    @Test
    void findMeThrowsNotFoundWhenUserHasNoProfessorProfile() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(99L);
        when(repository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findMe())
                .isInstanceOf(ProfessorNotFoundException.class);
    }

    // ── approve ───────────────────────────────────────────────────────────────

    @Test
    void approveChangesProfessorStatusToApproved() {
        var professor = buildProfessor(1L, Professor.Status.PENDING);
        when(repository.findById(1L)).thenReturn(Optional.of(professor));
        when(repository.save(professor)).thenReturn(professor);

        var result = service.approve(1L);

        assertThat(result.status()).isEqualTo(Professor.Status.APPROVED);
        verify(repository).save(professor);
    }

    @Test
    void approveThrowsStateExceptionWhenProfessorIsAlreadyApproved() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildProfessor(1L, Professor.Status.APPROVED)));

        assertThatThrownBy(() -> service.approve(1L))
                .isInstanceOf(ProfessorStateException.class)
                .hasMessageContaining("already approved");
    }

    @Test
    void approveThrowsNotFoundWhenProfessorDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approve(99L))
                .isInstanceOf(ProfessorNotFoundException.class);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void updateSavesProfessorWithNewValues() {
        var professor = buildProfessor(1L, Professor.Status.APPROVED);
        var request = new ProfessorUpdateRequest("New Name", "new@ibmec.br", "MAT-002", "LAT-002", Set.of(101L));

        when(repository.findById(1L)).thenReturn(Optional.of(professor));
        when(repository.existsByEmailAndIdNot("new@ibmec.br", 1L)).thenReturn(false);
        when(repository.existsByLattesNumberAndIdNot("LAT-002", 1L)).thenReturn(false);
        when(courseGateway.keepOnlyExistingCourseIds(Set.of(101L))).thenReturn(Set.of(101L));
        when(repository.save(professor)).thenReturn(professor);

        var result = service.update(1L, request);

        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.email()).isEqualTo("new@ibmec.br");
        verify(repository).save(professor);
    }

    @Test
    void updateThrowsConflictWhenEmailIsAlreadyInUse() {
        var professor = buildProfessor(1L, Professor.Status.APPROVED);
        var request = new ProfessorUpdateRequest("Ada", "taken@ibmec.br", null, "LAT-001", Set.of());

        when(repository.findById(1L)).thenReturn(Optional.of(professor));
        when(repository.existsByEmailAndIdNot("taken@ibmec.br", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(ProfessorConflictException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void updateThrowsConflictWhenLattesNumberIsAlreadyInUse() {
        var professor = buildProfessor(1L, Professor.Status.APPROVED);
        var request = new ProfessorUpdateRequest("Ada", "ada@ibmec.br", null, "LAT-TAKEN", Set.of());

        when(repository.findById(1L)).thenReturn(Optional.of(professor));
        when(repository.existsByEmailAndIdNot("ada@ibmec.br", 1L)).thenReturn(false);
        when(repository.existsByLattesNumberAndIdNot("LAT-TAKEN", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(ProfessorConflictException.class)
                .hasMessageContaining("Lattes");
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void deleteRemovesProfessor() {
        var professor = buildProfessor(1L, Professor.Status.APPROVED);
        when(repository.findById(1L)).thenReturn(Optional.of(professor));

        service.delete(1L);

        verify(repository).delete(professor);
    }

    @Test
    void deleteThrowsNotFoundWhenProfessorDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ProfessorNotFoundException.class);
    }

    // ── findProfessorPublications ─────────────────────────────────────────────

    @Test
    void findProfessorPublicationsReturnsGatewayResult() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildProfessor(1L, Professor.Status.APPROVED)));
        when(publicationsGateway.findPublicationsByProfessorId(1L)).thenReturn(List.of("pub1", "pub2"));

        var result = service.findProfessorPublications(1L);

        assertThat(result.professorId()).isEqualTo(1L);
        assertThat(result.publications()).hasSize(2);
    }

    @Test
    void findProfessorPublicationsThrowsIntegrationExceptionWhenGatewayFails() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildProfessor(1L, Professor.Status.APPROVED)));
        when(publicationsGateway.findPublicationsByProfessorId(1L))
                .thenThrow(new RuntimeException("gateway down"));

        assertThatThrownBy(() -> service.findProfessorPublications(1L))
                .isInstanceOf(ProfessorIntegrationException.class)
                .hasMessageContaining("publications");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Professor buildProfessor(Long id, Professor.Status status) {
        var professor = new Professor();
        setField(professor, "id", id);
        professor.setUserId(10L);
        professor.setName("Ada Lovelace");
        professor.setEmail("ada@ibmec.br");
        professor.setMatricula("MAT-001");
        professor.setLattesNumber("LAT-001");
        professor.setStatus(status);
        professor.setCourseIds(Set.of(101L, 102L));
        return professor;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
