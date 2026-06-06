package br.com.ibmec.researchstars.professor.dto;

import br.com.ibmec.researchstars.professor.Professor;
import java.time.LocalDateTime;
import java.util.Set;

public record ProfessorDetailResponse(
    Long id,
    Long userId,
    String name,
    String email,
    String lattesNumber,
    String matricula,
    Professor.Status status,
    Set<Long> courseIds,
    LocalDateTime createdAt
) {
}
