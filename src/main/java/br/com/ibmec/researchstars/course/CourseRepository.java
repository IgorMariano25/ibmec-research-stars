package br.com.ibmec.researchstars.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<Course> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);
}
