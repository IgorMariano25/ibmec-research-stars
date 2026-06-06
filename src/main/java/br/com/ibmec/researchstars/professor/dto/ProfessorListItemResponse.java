package br.com.ibmec.researchstars.professor.dto;

import br.com.ibmec.researchstars.professor.Professor;
import java.time.LocalDateTime;

public record ProfessorListItemResponse(
    Long id,
    String name,
    String email,
    String lattesNumber,
    String matricula,
    Professor.Status status,
    LocalDateTime createdAt
) {
}
