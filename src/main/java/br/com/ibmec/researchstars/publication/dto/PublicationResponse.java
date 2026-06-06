package br.com.ibmec.researchstars.publication.dto;

import br.com.ibmec.researchstars.publication.PublicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PublicationResponse(
        Long id,
        String title,
        String link,
        LocalDate publicationDate,
        PublicationStatus status,
        Long professorId,
        Long validatedByUserId,
        LocalDateTime validatedAt,
        LocalDateTime createdAt
) {}
