package br.com.ibmec.researchstars.course;

import br.com.ibmec.researchstars.course.dto.Course;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findById(@Nonnull Long id);
}
