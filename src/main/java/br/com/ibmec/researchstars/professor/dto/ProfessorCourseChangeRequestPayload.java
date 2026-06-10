package br.com.ibmec.researchstars.professor.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record ProfessorCourseChangeRequestPayload(
    @NotEmpty Set<Long> courseIds
) {
}
