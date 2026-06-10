package br.com.ibmec.researchstars.professor;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorCourseChangeRequestRepository
        extends JpaRepository<ProfessorCourseChangeRequest, Long> {

    Optional<ProfessorCourseChangeRequest> findFirstByProfessorIdAndStatus(
            Long professorId,
            ProfessorCourseChangeRequest.Status status
    );
}
