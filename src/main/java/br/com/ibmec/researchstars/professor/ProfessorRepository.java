package br.com.ibmec.researchstars.professor;

import br.com.ibmec.researchstars.user.dto.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    Optional<Professor> findByUser(User user);
    boolean existsByLattesNumber(String lattesNumber);
}
