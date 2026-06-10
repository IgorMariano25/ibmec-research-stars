package br.com.ibmec.researchstars.publication.service;

import br.com.ibmec.researchstars.professor.Professor;
import br.com.ibmec.researchstars.professor.ProfessorRepository;
import br.com.ibmec.researchstars.publication.Publication;
import br.com.ibmec.researchstars.publication.PublicationStatus;
import br.com.ibmec.researchstars.publication.dto.CreatePublicationRequest;
import br.com.ibmec.researchstars.publication.dto.PublicationRequest;
import br.com.ibmec.researchstars.publication.dto.PublicationResponse;
import br.com.ibmec.researchstars.publication.mapper.PublicationMapper;
import br.com.ibmec.researchstars.publication.repository.PublicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final ProfessorRepository professorRepository;
    private final PublicationMapper mapper;

    public PublicationService(
            PublicationRepository publicationRepository,
            ProfessorRepository professorRepository,
            PublicationMapper mapper
    ) {
        this.publicationRepository = publicationRepository;
        this.professorRepository = professorRepository;
        this.mapper = mapper;
    }

    // GET /publications (Admin) — RF-14, RF-20
    @Transactional(readOnly = true)
    public Page<PublicationResponse> findAll(PublicationStatus status, Long professorId, String q, Pageable pageable) {
        return publicationRepository
                .findAllWithFilters(status, professorId, q, pageable)
                .map(this::toResponse);
    }

    // GET /publications/me (Professor) — RF-12
    @Transactional(readOnly = true)
    public Page<PublicationResponse> findMyPublications(Long professorId, Pageable pageable) {
        return publicationRepository
                .findAllByProfessorId(professorId, pageable)
                .map(this::toResponse);
    }

    // GET /publications/{id} — RF-12
    @Transactional(readOnly = true)
    public PublicationResponse findById(Long id, Long callerProfessorId, boolean isAdmin) {
        var publication = getPublicationOrThrow(id);
        if (!isAdmin && !publication.getProfessorId().equals(callerProfessorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }
        return toResponse(publication);
    }

    // POST /publications (Professor) — RF-10, RF-11, RF-18
    @Transactional
    public PublicationResponse create(Long professorId, CreatePublicationRequest request) {
        var publication = new Publication();
        publication.setProfessorId(professorId);
        publication.setTitle(request.title());
        publication.setLink(request.link());
        publication.setPublicationDate(request.publicationDate());
        publication.setPublicationType(request.publicationType());
        publication.setAbntReference(request.abntReference());
        publication.setStatus(PublicationStatus.PENDING);
        return toResponse(publicationRepository.save(publication));
    }

    // PATCH /publications/{id} (Professor dono / Admin) — RF-13
    @Transactional
    public PublicationResponse update(Long id, Long callerProfessorId, boolean isAdmin, PublicationRequest request) {
        var publication = getPublicationOrThrow(id);

        if (!isAdmin && !publication.getProfessorId().equals(callerProfessorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }

        publication.setTitle(request.title());
        publication.setLink(request.link());
        publication.setPublicationDate(request.publicationDate());
        publication.setPublicationType(request.publicationType());
        publication.setAbntReference(request.abntReference());
        if (publication.getStatus() == PublicationStatus.VALIDATED) {
            publication.setStatus(PublicationStatus.PENDING);
            publication.setValidatedByUserId(null);
            publication.setValidatedAt(null);
        }
        return toResponse(publicationRepository.save(publication));
    }

    // POST /publications/{id}/validate (Admin) — RF-15
    @Transactional
    public PublicationResponse validate(Long id, Long adminUserId) {
        var publication = getPublicationOrThrow(id);
        publication.setStatus(PublicationStatus.VALIDATED);
        publication.setValidatedByUserId(adminUserId);
        publication.setValidatedAt(LocalDateTime.now());
        return toResponse(publicationRepository.save(publication));
    }

    // POST /publications/{id}/reject (Admin) — RF-16
    @Transactional
    public PublicationResponse reject(Long id, Long adminUserId) {
        var publication = getPublicationOrThrow(id);
        publication.setStatus(PublicationStatus.REJECTED);
        publication.setValidatedByUserId(adminUserId);
        publication.setValidatedAt(LocalDateTime.now());
        return toResponse(publicationRepository.save(publication));
    }

    // DELETE /publications/{id} (Professor dono / Admin) — RF-17
    @Transactional
    public void delete(Long id, Long callerProfessorId, boolean isAdmin) {
        var publication = getPublicationOrThrow(id);
        if (!isAdmin && !publication.getProfessorId().equals(callerProfessorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }
        publicationRepository.delete(publication);
    }

    private Publication getPublicationOrThrow(Long id) {
        return publicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicação não encontrada: " + id));
    }

    private PublicationResponse toResponse(Publication publication) {
        return mapper.toResponse(publication, resolveProfessorName(publication.getProfessorId()));
    }

    private String resolveProfessorName(Long professorId) {
        return professorRepository.findById(professorId)
                .map(Professor::getName)
                .orElse(null);
    }
}
