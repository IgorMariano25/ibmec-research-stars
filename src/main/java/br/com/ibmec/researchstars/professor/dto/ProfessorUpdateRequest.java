package br.com.ibmec.researchstars.professor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record ProfessorUpdateRequest(
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Email @Size(max = 255) String email,
    @Size(max = 100) String matricula,
    @NotBlank @Size(max = 100) String lattesNumber,
    @NotNull Set<Long> courseIds
) {
}
