package br.com.ibmec.researchstars.professor.dto;

import br.com.ibmec.researchstars.course.dto.CourseDto;
import br.com.ibmec.researchstars.professor.ProfessorCourseChangeRequest;
import java.time.LocalDateTime;
import java.util.List;

public record ProfessorCourseChangeRequestDto(
    Long id,
    Long professorId,
    List<CourseDto> requestedCourses,
    ProfessorCourseChangeRequest.Status status,
    Long requestedByUserId,
    Long reviewedByUserId,
    LocalDateTime requestedAt,
    LocalDateTime reviewedAt
) {
}
