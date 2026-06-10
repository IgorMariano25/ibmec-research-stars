package br.com.ibmec.researchstars.professor;

import br.com.ibmec.researchstars.course.CourseRepository;
import br.com.ibmec.researchstars.course.dto.CourseDto;
import br.com.ibmec.researchstars.professor.dto.PagedResponse;
import br.com.ibmec.researchstars.professor.dto.ProfessorApproveResponse;
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

import java.util.List;
import java.util.Map;
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

    public ProfessorService(
        ProfessorRepository repository,
        CurrentUserProvider currentUserProvider,
        CourseGateway courseGateway,
        ProfessorPublicationsGateway publicationsGateway,
        CourseRepository courseRepository
    ) {
        this.repository = repository;
        this.currentUserProvider = currentUserProvider;
        this.courseGateway = courseGateway;
        this.publicationsGateway = publicationsGateway;
        this.courseRepository = courseRepository;
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
        var validCourseIds = courseGateway.keepOnlyExistingCourseIds(request.courseIds());
        professor.setCourseIds(validCourseIds);

        return toDetail(repository.save(professor));
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
        return ProfessorMapper.toDetail(professor, courses);
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
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private Professor getProfessorOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProfessorNotFoundException(id));
    }
}
