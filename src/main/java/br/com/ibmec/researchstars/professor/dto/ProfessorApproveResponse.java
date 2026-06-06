package br.com.ibmec.researchstars.professor.dto;

import br.com.ibmec.researchstars.professor.Professor;

public record ProfessorApproveResponse(Long id, Professor.Status status) {
}
