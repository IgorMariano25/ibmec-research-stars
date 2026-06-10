package br.com.ibmec.researchstars.publication.repository;

import br.com.ibmec.researchstars.publication.Publication;
import br.com.ibmec.researchstars.publication.PublicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {

    // Admin: listar todas com filtros opcionais (RF-14, RF-20)
    @Query("""
            SELECT p FROM Publication p
            WHERE (:status IS NULL OR p.status = :status)
              AND (:professorId IS NULL OR p.professorId = :professorId)
              AND (:q IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Publication> findAllWithFilters(
            @Param("status") PublicationStatus status,
            @Param("professorId") Long professorId,
            @Param("q") String q,
            Pageable pageable
    );

    // Professor: listar apenas as próprias publicações (RF-12)
    Page<Publication> findAllByProfessorId(Long professorId, Pageable pageable);

    // Usado pelo ranking/compliance (RN-01)
    @Query("""
            SELECT COUNT(p) FROM Publication p
            WHERE p.professorId = :professorId
              AND p.status = 'VALIDATED'
              AND p.publicationDate >= :since
            """)
    long countValidatedSince(@Param("professorId") Long professorId, @Param("since") LocalDate since);

    @Query("""
            SELECT COUNT(p) FROM Publication p
            WHERE p.professorId = :professorId
              AND p.status = 'VALIDATED'
              AND p.publicationDate BETWEEN :startDate AND :endDate
            """)
    long countValidatedBetween(
            @Param("professorId") Long professorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Admin: produção científica completa de um professor (RF-19)
    List<Publication> findAllByProfessorId(Long professorId);
}
