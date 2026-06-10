package br.com.ibmec.researchstars.professor;

import br.com.ibmec.researchstars.course.CourseRepository;
import br.com.ibmec.researchstars.course.dto.CourseDto;
import br.com.ibmec.researchstars.professor.dto.PagedResponse;
import br.com.ibmec.researchstars.professor.dto.ProfessorApproveResponse;
import br.com.ibmec.researchstars.professor.dto.ProfessorCourseChangeRequestDto;
import br.com.ibmec.researchstars.professor.dto.ProfessorCourseChangeRequestPayload;
import br.com.ibmec.researchstars.professor.dto.ProfessorDetailResponse;
import br.com.ibmec.researchstars.professor.dto.ProfessorListItemResponse;
import br.com.ibmec.researchstars.professor.dto.ProfessorPublicationsResponse;
import br.com.ibmec.researchstars.professor.dto.ProfessorUpdateRequest;
import br.com.ibmec.researchstars.professor.exception.ProfessorConflictException;
import br.com.ibmec.researchstars.professor.exception.ProfessorIntegrationException;
import br.com.ibmec.researchstars.professor.exception.ProfessorNotFoundException;
import br.com.ibmec.researchstars.professor.exception.ProfessorStateException;
import br.com.ibmec.researchstars.professor.integration.CourseGateway;
import br.com.ibmec.researchstars.professor.integration.CurrentUserProvider;
import br.com.ibmec.researchstars.professor.integration.ProfessorPublicationsGateway;
import br.com.ibmec.researchstars.professor.mapper.ProfessorMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProfessorService {

    private final ProfessorRepository repository;
    private final CurrentUserProvider currentUserProvider;
    private final CourseGateway courseGateway;
    private final ProfessorPublicationsGateway publicationsGateway;
    private final CourseRepository courseRepository;
    private final ProfessorCourseChangeRequestRepository courseChangeRequestRepository;
    private final Clock clock;

    public ProfessorService(
        ProfessorRepository repository,
        CurrentUserProvider currentUserProvider,
        CourseGateway courseGateway,
        ProfessorPublicationsGateway publicationsGateway,
        CourseRepository courseRepository,
        ProfessorCourseChangeRequestRepository courseChangeRequestRepository,
        Clock clock
    ) {
        this.repository = repository;
        this.currentUserProvider = currentUserProvider;
        this.courseGateway = courseGateway;
        this.publicationsGateway = publicationsGateway;
        this.courseRepository = courseRepository;
        this.courseChangeRequestRepository = courseChangeRequestRepository;
        this.clock = clock;
    }

    public PagedResponse<ProfessorListItemResponse> list(Professor.Status status, String q, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        var result = repository.findAll(ProfessorSpecifications.byFilters(status, q), pageable);
        var coursesById = loadCoursesById(result.getContent());
        var content = result.getContent().stream()
                .map(professor -> ProfessorMapper.toListItem(professor, mapCourses(professor, coursesById)))
                .toList();
        return new PagedResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    public ProfessorDetailResponse findById(Long id) {
        return toDetail(getProfessorOrThrow(id));
    }

    public ProfessorDetailResponse findMe() {
        var userId = currentUserProvider.getCurrentUserId();
        var professor = repository.findByUserId(userId)
            .orElseThrow(() -> new ProfessorNotFoundException("Professor not found for user: " + userId));
        return toDetail(professor);
    }

    @Transactional
    public ProfessorApproveResponse approve(Long id) {
        var professor = getProfessorOrThrow(id);
        if (professor.getStatus() == Professor.Status.APPROVED) {
            throw new ProfessorStateException("Professor is already approved");
        }
        professor.setStatus(Professor.Status.APPROVED);
        applyPendingCourseChangeIfPresent(professor, currentUserProvider.getCurrentUserId());
        return ProfessorMapper.toApproveResponse(repository.save(professor));
    }

    @Transactional
    public ProfessorDetailResponse update(Long id, ProfessorUpdateRequest request) {
        var professor = getProfessorOrThrow(id);

        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ProfessorConflictException("Email already in use");
        }
        if (repository.existsByLattesUrlAndIdNot(request.lattesUrl(), id)) {
            throw new ProfessorConflictException("Lattes URL already in use");
        }

        professor.setName(request.name());
        professor.setEmail(request.email());
        professor.setMatricula(request.matricula());
        professor.setLattesUrl(request.lattesUrl());
        var validCourseIds = validateCourseIds(request.courseIds());
        professor.setCourseIds(validCourseIds);
        supersedePendingCourseChangeIfPresent(professor.getId(), currentUserProvider.getCurrentUserId());

        return toDetail(repository.save(professor));
    }

    @Transactional
    public ProfessorDetailResponse requestMyCourseChange(ProfessorCourseChangeRequestPayload request) {
        var userId = currentUserProvider.getCurrentUserId();
        var professor = repository.findByUserId(userId)
                .orElseThrow(() -> new ProfessorNotFoundException("Professor not found for user: " + userId));

        if (findPendingCourseChange(professor.getId()).isPresent()) {
            throw new ProfessorStateException("Professor already has a pending course change request");
        }

        var changeRequest = new ProfessorCourseChangeRequest();
        changeRequest.setProfessorId(professor.getId());
        changeRequest.setRequestedByUserId(userId);
        changeRequest.setRequestedCourseIds(validateCourseIds(request.courseIds()));
        courseChangeRequestRepository.save(changeRequest);

        return toDetail(professor);
    }

    @Transactional
    public ProfessorDetailResponse approveCourseChange(Long id) {
        var professor = getProfessorOrThrow(id);
        var changeRequest = findPendingCourseChange(id)
                .orElseThrow(() -> new ProfessorStateException("Professor has no pending course change request"));
        approveCourseChange(professor, changeRequest, currentUserProvider.getCurrentUserId());
        repository.save(professor);
        return toDetail(professor);
    }

    @Transactional
    public ProfessorDetailResponse rejectCourseChange(Long id) {
        var professor = getProfessorOrThrow(id);
        var changeRequest = findPendingCourseChange(id)
                .orElseThrow(() -> new ProfessorStateException("Professor has no pending course change request"));
        reviewCourseChange(changeRequest, ProfessorCourseChangeRequest.Status.REJECTED, currentUserProvider.getCurrentUserId());
        courseChangeRequestRepository.save(changeRequest);
        return toDetail(professor);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getProfessorOrThrow(id));
    }

    public ProfessorPublicationsResponse findProfessorPublications(Long id) {
        getProfessorOrThrow(id);
        try {
            var publications = publicationsGateway.findPublicationsByProfessorId(id);
            return new ProfessorPublicationsResponse(id, publications);
        } catch (RuntimeException exception) {
            throw new ProfessorIntegrationException("Error querying professor publications", exception);
        }
    }

    // Carrega os objetos Course completos e monta o DTO
    private ProfessorDetailResponse toDetail(Professor professor) {
        List<CourseDto> courses = courseRepository.findAllById(professor.getCourseIds())
                .stream()
                .map(c -> new CourseDto(c.getId(), c.getName(), c.getCode()))
                .toList();
        var pendingCourseChange = findPendingCourseChange(professor.getId())
                .map(this::toCourseChangeRequestDto)
                .orElse(null);
        return ProfessorMapper.toDetail(professor, courses, pendingCourseChange);
    }

    private Map<Long, CourseDto> loadCoursesById(List<Professor> professors) {
        Set<Long> courseIds = professors.stream()
                .flatMap(professor -> professor.getCourseIds().stream())
                .collect(Collectors.toSet());
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        return courseRepository.findAllById(courseIds).stream()
                .map(course -> new CourseDto(course.getId(), course.getName(), course.getCode()))
                .collect(Collectors.toMap(CourseDto::id, Function.identity()));
    }

    private List<CourseDto> mapCourses(Professor professor, Map<Long, CourseDto> coursesById) {
        return professor.getCourseIds().stream()
                .map(coursesById::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private Set<Long> validateCourseIds(Set<Long> courseIds) {
        var validCourseIds = courseGateway.keepOnlyExistingCourseIds(courseIds);
        if (validCourseIds.size() != courseIds.size()) {
            throw new ProfessorStateException("One or more courses were not found");
        }
        return validCourseIds;
    }

    private void applyPendingCourseChangeIfPresent(Professor professor, Long reviewedByUserId) {
        findPendingCourseChange(professor.getId())
                .ifPresent(changeRequest -> approveCourseChange(professor, changeRequest, reviewedByUserId));
    }

    private void approveCourseChange(
            Professor professor,
            ProfessorCourseChangeRequest changeRequest,
            Long reviewedByUserId
    ) {
        professor.setCourseIds(changeRequest.getRequestedCourseIds());
        reviewCourseChange(changeRequest, ProfessorCourseChangeRequest.Status.APPROVED, reviewedByUserId);
        courseChangeRequestRepository.save(changeRequest);
    }

    private void supersedePendingCourseChangeIfPresent(Long professorId, Long reviewedByUserId) {
        findPendingCourseChange(professorId)
                .ifPresent(changeRequest -> {
                    reviewCourseChange(changeRequest, ProfessorCourseChangeRequest.Status.SUPERSEDED, reviewedByUserId);
                    courseChangeRequestRepository.save(changeRequest);
                });
    }

    private void reviewCourseChange(
            ProfessorCourseChangeRequest changeRequest,
            ProfessorCourseChangeRequest.Status status,
            Long reviewedByUserId
    ) {
        changeRequest.setStatus(status);
        changeRequest.setReviewedByUserId(reviewedByUserId);
        changeRequest.setReviewedAt(LocalDateTime.now(clock));
    }

    private java.util.Optional<ProfessorCourseChangeRequest> findPendingCourseChange(Long professorId) {
        return courseChangeRequestRepository.findFirstByProfessorIdAndStatus(
                professorId,
                ProfessorCourseChangeRequest.Status.PENDING
        );
    }

    private ProfessorCourseChangeRequestDto toCourseChangeRequestDto(ProfessorCourseChangeRequest changeRequest) {
        return new ProfessorCourseChangeRequestDto(
                changeRequest.getId(),
                changeRequest.getProfessorId(),
                courseRepository.findAllById(changeRequest.getRequestedCourseIds()).stream()
                        .map(course -> new CourseDto(course.getId(), course.getName(), course.getCode()))
                        .toList(),
                changeRequest.getStatus(),
                changeRequest.getRequestedByUserId(),
                changeRequest.getReviewedByUserId(),
                changeRequest.getRequestedAt(),
                changeRequest.getReviewedAt()
        );
    }

    private Professor getProfessorOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProfessorNotFoundException(id));
    }
}
