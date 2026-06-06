package br.com.ibmec.researchstars.professor;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProfessorRepository extends JpaRepository<Professor, Long>, JpaSpecificationExecutor<Professor> {

    Optional<Professor> findByUserId(Long userId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByLattesNumber(String lattesNumber);

    boolean existsByLattesNumberAndIdNot(String lattesNumber, Long id);
}
