package br.com.ibmec.researchstars.publication.controller;

import br.com.ibmec.researchstars.auth.AppUserDetails;
import br.com.ibmec.researchstars.publication.PublicationStatus;
import br.com.ibmec.researchstars.publication.dto.CreatePublicationRequest;
import br.com.ibmec.researchstars.publication.dto.PublicationRequest;
import br.com.ibmec.researchstars.publication.dto.PublicationResponse;
import br.com.ibmec.researchstars.publication.service.PublicationService;
import br.com.ibmec.researchstars.user.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/publications")
public class PublicationController {

    private final PublicationService publicationService;

    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    // GET /publications — Admin (RF-14, RF-20)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PublicationResponse>> findAll(
            @RequestParam(required = false) PublicationStatus status,
            @RequestParam(required = false) Long professorId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(publicationService.findAll(status, professorId, q, pageable));
    }

    // GET /publications/me — Professor (RF-12)
    @GetMapping("/me")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Page<PublicationResponse>> findMyPublications(
            @AuthenticationPrincipal AppUserDetails principal,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(publicationService.findMyPublications(principal.getProfessorId(), pageable));
    }

    // GET /publications/{id} — Admin / dono (RF-12)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR')")
    public ResponseEntity<PublicationResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal
    ) {
        boolean isAdmin = principal.getRole() == User.Role.ADMIN;
        return ResponseEntity.ok(publicationService.findById(id, principal.getProfessorId(), isAdmin));
    }

    // POST /publications — Professor (RF-10, RF-11)
    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<PublicationResponse> create(
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody CreatePublicationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(publicationService.create(principal.getProfessorId(), request));
    }

    // PATCH /publications/{id} — Dono / Admin (RF-13)
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR')")
    public ResponseEntity<PublicationResponse> update(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody PublicationRequest request
    ) {
        boolean isAdmin = principal.getRole() == User.Role.ADMIN;
        return ResponseEntity.ok(publicationService.update(id, principal.getProfessorId(), isAdmin, request));
    }

    // POST /publications/{id}/validate — Admin (RF-15)
    @PostMapping("/{id}/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PublicationResponse> validate(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal
    ) {
        return ResponseEntity.ok(publicationService.validate(id, principal.getUserId()));
    }

    // POST /publications/{id}/reject — Admin (RF-16)
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PublicationResponse> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal
    ) {
        return ResponseEntity.ok(publicationService.reject(id, principal.getUserId()));
    }

    // DELETE /publications/{id} — Dono / Admin (RF-17)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal
    ) {
        boolean isAdmin = principal.getRole() == User.Role.ADMIN;
        publicationService.delete(id, principal.getProfessorId(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
