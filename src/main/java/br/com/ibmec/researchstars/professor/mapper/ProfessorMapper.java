package br.com.ibmec.researchstars.professor.mapper;

import br.com.ibmec.researchstars.professor.Professor;
import br.com.ibmec.researchstars.professor.dto.ProfessorApproveResponse;
import br.com.ibmec.researchstars.professor.dto.ProfessorDetailResponse;
import br.com.ibmec.researchstars.professor.dto.ProfessorListItemResponse;
import java.util.HashSet;

public final class ProfessorMapper {

    private ProfessorMapper() {
    }

    public static ProfessorListItemResponse toListItem(Professor professor) {
        return new ProfessorListItemResponse(
            professor.getId(),
            professor.getName(),
            professor.getEmail(),
            professor.getLattesNumber(),
            professor.getMatricula(),
            professor.getStatus(),
            professor.getCreatedAt()
        );
    }

    public static ProfessorDetailResponse toDetail(Professor professor) {
        return new ProfessorDetailResponse(
            professor.getId(),
            professor.getUserId(),
            professor.getName(),
            professor.getEmail(),
            professor.getLattesNumber(),
            professor.getMatricula(),
            professor.getStatus(),
            new HashSet<>(professor.getCourseIds()),
            professor.getCreatedAt()
        );
    }

    public static ProfessorApproveResponse toApproveResponse(Professor professor) {
        return new ProfessorApproveResponse(professor.getId(), professor.getStatus());
    }
}
