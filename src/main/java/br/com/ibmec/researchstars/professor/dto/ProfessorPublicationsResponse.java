package br.com.ibmec.researchstars.professor.dto;

import java.util.List;

public record ProfessorPublicationsResponse(Long professorId, List<Object> publications) {
}
