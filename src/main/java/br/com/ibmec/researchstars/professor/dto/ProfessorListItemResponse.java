package br.com.ibmec.researchstars.professor.dto;

import br.com.ibmec.researchstars.course.dto.CourseDto;
import br.com.ibmec.researchstars.professor.Professor;
import java.time.LocalDateTime;
import java.util.List;

public record ProfessorListItemResponse(
    Long id,
    String name,
    String email,
    String lattesUrl,
    String matricula,
    Professor.Status status,
    List<CourseDto> courses,
    LocalDateTime createdAt
) {
}
