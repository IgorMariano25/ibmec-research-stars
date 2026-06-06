package br.com.ibmec.researchstars.publication.service;

import br.com.ibmec.researchstars.publication.Publication;
import br.com.ibmec.researchstars.publication.PublicationStatus;
import br.com.ibmec.researchstars.publication.dto.PublicationCreateRequest;
import br.com.ibmec.researchstars.publication.dto.PublicationResponse;
import br.com.ibmec.researchstars.publication.mapper.PublicationMapper;
import br.com.ibmec.researchstars.publication.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final PublicationMapper mapper;

    // GET /publications (Admin) — RF-14, RF-20
    @Transactional(readOnly = true)
    public Page<PublicationResponse> findAll(PublicationStatus status, Long professorId, String q, Pageable pageable) {
        return publicationRepository
                .findAllWithFilters(status, professorId, q, pageable)
                .map(mapper::toResponse);
    }

    // GET /publications/me (Professor) — RF-12
    @Transactional(readOnly = true)
    public Page<PublicationResponse> findMyPublications(Long professorId, Pageable pageable) {
        return publicationRepository
                .findAllByProfessorId(professorId, pageable)
                .map(mapper::toResponse);
    }

    // GET /publications/{id} (Admin / dono) — RF-12
    @Transactional(readOnly = true)
    public PublicationResponse findById(Long id) {
        return publicationRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicação não encontrada: " + id));
    }

    // POST /publications (Professor) — RF-10, RF-11, RF-18
    @Transactional
    public PublicationResponse create(Long professorId, PublicationCreateRequest request) {
        var publication = new Publication();
        publication.setProfessorId(professorId);
        publication.setTitle(request.title());
        publication.setLink(request.link());
        publication.setPublicationDate(request.publicationDate());
        publication.setStatus(PublicationStatus.PENDING);

        return mapper.toResponse(publicationRepository.save(publication));
    }
}