package br.com.ibmec.researchstars.publication.dto;

import br.com.ibmec.researchstars.publication.PublicationStatus;
import br.com.ibmec.researchstars.publication.PublicationType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PublicationResponse(
        Long id,
        String title,
        String link,
        LocalDate publicationDate,
        PublicationType publicationType,
        String abntReference,
        PublicationStatus status,
        Long professorId,
        String professorName,
        Long validatedByUserId,
        LocalDateTime validatedAt,
        LocalDateTime createdAt
) {}
