package br.com.ibmec.researchstars.professor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.ibmec.researchstars.course.Course;
import br.com.ibmec.researchstars.config.DateTimeConfig;
import br.com.ibmec.researchstars.professor.dto.ProfessorCourseChangeRequestPayload;
import br.com.ibmec.researchstars.professor.dto.ProfessorUpdateRequest;
import br.com.ibmec.researchstars.professor.exception.ProfessorConflictException;
import br.com.ibmec.researchstars.professor.exception.ProfessorIntegrationException;
import br.com.ibmec.researchstars.professor.exception.ProfessorNotFoundException;
import br.com.ibmec.researchstars.professor.exception.ProfessorStateException;
import br.com.ibmec.researchstars.professor.integration.CourseGateway;
import br.com.ibmec.researchstars.professor.integration.CurrentUserProvider;
import br.com.ibmec.researchstars.professor.integration.ProfessorPublicationsGateway;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

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
    private br.com.ibmec.researchstars.course.CourseRepository courseRepository;

    @Mock
    private ProfessorCourseChangeRequestRepository courseChangeRequestRepository;

    private ProfessorService service;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-06-10T15:00:00Z"),
            DateTimeConfig.SAO_PAULO_ZONE
    );

    @BeforeEach
    void setUp() {
        service = new ProfessorService(
                repository,
                currentUserProvider,
                courseGateway,
                publicationsGateway,
                courseRepository,
                courseChangeRequestRepository,
                clock
        );
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

    @Test
    @SuppressWarnings("unchecked")
    void listReturnsCoursesForEachProfessor() {
        var professor = buildProfessor(1L, Professor.Status.APPROVED);
        var softwareEngineering = buildCourse(101L, "Engenharia de Software", "ES01");
        var dataScience = buildCourse(102L, "CDIA", "CDIA01");
        when(repository.findAll(any(Specification.class), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(professor)));
        when(courseRepository.findAllById(Set.of(101L, 102L))).thenReturn(List.of(softwareEngineering, dataScience));

        var result = service.list(null, null, 0, 10);

        assertThat(result.content().get(0).courses())
            .extracting("code")
            .containsExactlyInAnyOrder("ES01", "CDIA01");
    }

    @Test
    @SuppressWarnings("unchecked")
    void listReturnsEmptyCourseListWhenProfessorHasNoCourses() {
        var professor = buildProfessor(1L, Professor.Status.APPROVED);
        professor.setCourseIds(Set.of());
        when(repository.findAll(any(Specification.class), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(professor)));

        var result = service.list(null, null, 0, 10);

        assertThat(result.content().get(0).courses()).isEmpty();
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findByIdReturnsProfessorDetail() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildProfessor(1L, Professor.Status.APPROVED)));
        when(courseChangeRequestRepository.findFirstByProfessorIdAndStatus(1L, ProfessorCourseChangeRequest.Status.PENDING))
                .thenReturn(Optional.empty());

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
        when(courseChangeRequestRepository.findFirstByProfessorIdAndStatus(1L, ProfessorCourseChangeRequest.Status.PENDING))
                .thenReturn(Optional.empty());

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
        when(courseChangeRequestRepository.findFirstByProfessorIdAndStatus(1L, ProfessorCourseChangeRequest.Status.PENDING))
                .thenReturn(Optional.empty());
        when(repository.save(professor)).thenReturn(professor);

        var result = service.approve(1L);

        assertThat(result.status()).isEqualTo(Professor.Status.APPROVED);
        verify(repository).save(professor);
    }

    @Test
    void approveAppliesPendingCourseChangeRequest() {
        var professor = buildProfessor(1L, Professor.Status.PENDING);
        professor.setCourseIds(Set.of());
        var changeRequest = buildCourseChangeRequest(20L, 1L, 10L, Set.of(101L, 102L));
        when(repository.findById(1L)).thenReturn(Optional.of(professor));
        when(currentUserProvider.getCurrentUserId()).thenReturn(99L);
        when(courseChangeRequestRepository.findFirstByProfessorIdAndStatus(1L, ProfessorCourseChangeRequest.Status.PENDING))
                .thenReturn(Optional.of(changeRequest));
        when(repository.save(professor)).thenReturn(professor);

        service.approve(1L);

        assertThat(professor.getStatus()).isEqualTo(Professor.Status.APPROVED);
        assertThat(professor.getCourseIds()).containsExactlyInAnyOrder(101L, 102L);
        assertThat(professor.getCourseIds()).isNotSameAs(changeRequest.getRequestedCourseIds());
        assertThat(changeRequest.getStatus()).isEqualTo(ProfessorCourseChangeRequest.Status.APPROVED);
        assertThat(changeRequest.getReviewedByUserId()).isEqualTo(99L);
        assertThat(changeRequest.getReviewedAt())
                .isEqualTo(LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
        verify(courseChangeRequestRepository).save(changeRequest);
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
        var request = new ProfessorUpdateRequest(
            "New Name",
            "new@ibmec.br",
            "MAT-002",
            "https://lattes.cnpq.br/2222222222222222",
            Set.of(101L)
        );

        when(repository.findById(1L)).thenReturn(Optional.of(professor));
        when(repository.existsByEmailAndIdNot("new@ibmec.br", 1L)).thenReturn(false);
        when(repository.existsByLattesUrlAndIdNot("https://lattes.cnpq.br/2222222222222222", 1L)).thenReturn(false);
        when(courseGateway.keepOnlyExistingCourseIds(Set.of(101L))).thenReturn(Set.of(101L));
        when(currentUserProvider.getCurrentUserId()).thenReturn(99L);
        when(courseChangeRequestRepository.findFirstByProfessorIdAndStatus(1L, ProfessorCourseChangeRequest.Status.PENDING))
                .thenReturn(Optional.empty());
        when(repository.save(professor)).thenReturn(professor);

        var result = service.update(1L, request);

        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.email()).isEqualTo("new@ibmec.br");
        verify(repository).save(professor);
    }

    @Test
    void updateSupersedesPendingCourseChangeRequest() {
        var professor = buildProfessor(1L, Professor.Status.APPROVED);
        var request = new ProfessorUpdateRequest(
            "Ada Lovelace",
            "ada@ibmec.br",
            "MAT-001",
            "https://lattes.cnpq.br/1111111111111111",
            Set.of(102L)
        );
        var changeRequest = buildCourseChangeRequest(20L, 1L, 10L, Set.of(101L));
        when(repository.findById(1L)).thenReturn(Optional.of(professor));
        when(repository.existsByEmailAndIdNot("ada@ibmec.br", 1L)).thenReturn(false);
        when(repository.existsByLattesUrlAndIdNot("https://lattes.cnpq.br/1111111111111111", 1L))
                .thenReturn(false);
        when(courseGateway.keepOnlyExistingCourseIds(Set.of(102L))).thenReturn(Set.of(102L));
        when(currentUserProvider.getCurrentUserId()).thenReturn(99L);
        when(courseChangeRequestRepository.findFirstByProfessorIdAndStatus(1L, ProfessorCourseChangeRequest.Status.PENDING))
                .thenReturn(Optional.of(changeRequest), Optional.empty());
        when(repository.save(professor)).thenReturn(professor);

        service.update(1L, request);

        assertThat(professor.getCourseIds()).containsExactly(102L);
        assertThat(changeRequest.getStatus()).isEqualTo(ProfessorCourseChangeRequest.Status.SUPERSEDED);
        assertThat(changeRequest.getReviewedByUserId()).isEqualTo(99L);
        verify(courseChangeRequestRepository).save(changeRequest);
    }

    @Test
    void updateThrowsConflictWhenEmailIsAlreadyInUse() {
        var professor = buildProfessor(1L, Professor.Status.APPROVED);
        var request = new ProfessorUpdateRequest(
            "Ada",
            "taken@ibmec.br",
            null,
            "https://lattes.cnpq.br/1111111111111111",
            Set.of()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(professor));
        when(repository.existsByEmailAndIdNot("taken@ibmec.br", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request))
            .isInstanceOf(ProfessorConflictException.class)
            .hasMessageContaining("Email");
    }

    @Test
    void updateThrowsConflictWhenLattesUrlIsAlreadyInUse() {
        var professor = buildProfessor(1L, Professor.Status.APPROVED);
        var request = new ProfessorUpdateRequest(
            "Ada",
            "ada@ibmec.br",
            null,
            "https://lattes.cnpq.br/taken",
            Set.of()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(professor));
        when(repository.existsByEmailAndIdNot("ada@ibmec.br", 1L)).thenReturn(false);
        when(repository.existsByLattesUrlAndIdNot("https://lattes.cnpq.br/taken", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request))
            .isInstanceOf(ProfessorConflictException.class)
            .hasMessageContaining("Lattes");
    }

    // ── course change requests ───────────────────────────────────────────────

    @Test
    void requestMyCourseChangeCreatesPendingRequestWithoutChangingActiveCourses() {
        var professor = buildProfessor(1L, Professor.Status.APPROVED);
        var request = new ProfessorCourseChangeRequestPayload(Set.of(102L));
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(repository.findByUserId(10L)).thenReturn(Optional.of(professor));
        when(courseChangeRequestRepository.findFirstByProfessorIdAndStatus(1L, ProfessorCourseChangeRequest.Status.PENDING))
                .thenReturn(Optional.empty());
        when(courseGateway.keepOnlyExistingCourseIds(Set.of(102L))).thenReturn(Set.of(102L));

        service.requestMyCourseChange(request);

        assertThat(professor.getCourseIds()).containsExactlyInAnyOrder(101L, 102L);
        verify(courseChangeRequestRepository).save(org.mockito.ArgumentMatchers.argThat(changeRequest ->
                changeRequest.getProfessorId().equals(1L)
                        && changeRequest.getRequestedByUserId().equals(10L)
                        && changeRequest.getRequestedCourseIds().equals(Set.of(102L))
                        && changeRequest.getStatus() == ProfessorCourseChangeRequest.Status.PENDING
        ));
    }

    @Test
    void requestMyCourseChangeThrowsStateExceptionWhenPendingRequestExists() {
        var professor = buildProfessor(1L, Professor.Status.APPROVED);
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(repository.findByUserId(10L)).thenReturn(Optional.of(professor));
        when(courseChangeRequestRepository.findFirstByProfessorIdAndStatus(1L, ProfessorCourseChangeRequest.Status.PENDING))
                .thenReturn(Optional.of(buildCourseChangeRequest(20L, 1L, 10L, Set.of(102L))));

        assertThatThrownBy(() -> service.requestMyCourseChange(
                new ProfessorCourseChangeRequestPayload(Set.of(102L))
        ))
                .isInstanceOf(ProfessorStateException.class)
                .hasMessageContaining("pending course change");
    }

    @Test
    void requestMyCourseChangeRejectsUnknownCourseIds() {
        var professor = buildProfessor(1L, Professor.Status.APPROVED);
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(repository.findByUserId(10L)).thenReturn(Optional.of(professor));
        when(courseChangeRequestRepository.findFirstByProfessorIdAndStatus(1L, ProfessorCourseChangeRequest.Status.PENDING))
                .thenReturn(Optional.empty());
        when(courseGateway.keepOnlyExistingCourseIds(Set.of(999L))).thenReturn(Set.of());

        assertThatThrownBy(() -> service.requestMyCourseChange(
                new ProfessorCourseChangeRequestPayload(Set.of(999L))
        ))
                .isInstanceOf(ProfessorStateException.class)
                .hasMessageContaining("courses");
    }

    @Test
    void approveCourseChangeUpdatesActiveCourses() {
        var professor = buildProfessor(1L, Professor.Status.APPROVED);
        professor.setCourseIds(Set.of(101L));
        var changeRequest = buildCourseChangeRequest(20L, 1L, 10L, Set.of(102L));
        when(repository.findById(1L)).thenReturn(Optional.of(professor));
        when(currentUserProvider.getCurrentUserId()).thenReturn(99L);
        when(courseChangeRequestRepository.findFirstByProfessorIdAndStatus(1L, ProfessorCourseChangeRequest.Status.PENDING))
                .thenReturn(Optional.of(changeRequest), Optional.empty());
        when(repository.save(professor)).thenReturn(professor);

        service.approveCourseChange(1L);

        assertThat(professor.getCourseIds()).containsExactly(102L);
        assertThat(changeRequest.getStatus()).isEqualTo(ProfessorCourseChangeRequest.Status.APPROVED);
        assertThat(changeRequest.getReviewedByUserId()).isEqualTo(99L);
        verify(courseChangeRequestRepository).save(changeRequest);
    }

    @Test
    void rejectCourseChangeLeavesActiveCoursesUnchanged() {
        var professor = buildProfessor(1L, Professor.Status.APPROVED);
        professor.setCourseIds(Set.of(101L));
        var changeRequest = buildCourseChangeRequest(20L, 1L, 10L, Set.of(102L));
        when(repository.findById(1L)).thenReturn(Optional.of(professor));
        when(currentUserProvider.getCurrentUserId()).thenReturn(99L);
        when(courseChangeRequestRepository.findFirstByProfessorIdAndStatus(1L, ProfessorCourseChangeRequest.Status.PENDING))
                .thenReturn(Optional.of(changeRequest), Optional.empty());

        service.rejectCourseChange(1L);

        assertThat(professor.getCourseIds()).containsExactly(101L);
        assertThat(changeRequest.getStatus()).isEqualTo(ProfessorCourseChangeRequest.Status.REJECTED);
        assertThat(changeRequest.getReviewedByUserId()).isEqualTo(99L);
        verify(courseChangeRequestRepository).save(changeRequest);
    }

    @Test
    void approveCourseChangeThrowsStateExceptionWhenNoPendingRequestExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildProfessor(1L, Professor.Status.APPROVED)));
        when(courseChangeRequestRepository.findFirstByProfessorIdAndStatus(1L, ProfessorCourseChangeRequest.Status.PENDING))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approveCourseChange(1L))
                .isInstanceOf(ProfessorStateException.class)
                .hasMessageContaining("no pending course change");
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
        professor.setLattesUrl("https://lattes.cnpq.br/1111111111111111");
        professor.setStatus(status);
        professor.setCourseIds(Set.of(101L, 102L));
        return professor;
    }

    private Course buildCourse(Long id, String name, String code) {
        var course = new Course(name, code);
        setField(course, "id", id);
        return course;
    }

    private ProfessorCourseChangeRequest buildCourseChangeRequest(
            Long id,
            Long professorId,
            Long requestedByUserId,
            Set<Long> courseIds
    ) {
        var changeRequest = new ProfessorCourseChangeRequest();
        setField(changeRequest, "id", id);
        changeRequest.setProfessorId(professorId);
        changeRequest.setRequestedByUserId(requestedByUserId);
        changeRequest.setRequestedCourseIds(courseIds);
        return changeRequest;
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
