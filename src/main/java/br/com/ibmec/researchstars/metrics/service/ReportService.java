package br.com.ibmec.researchstars.metrics.service;

import br.com.ibmec.researchstars.course.Course;
import br.com.ibmec.researchstars.course.CourseRepository;
import br.com.ibmec.researchstars.metrics.dto.CourseComplianceDto;
import br.com.ibmec.researchstars.professor.Professor;
import br.com.ibmec.researchstars.professor.ProfessorRepository;
import br.com.ibmec.researchstars.publication.repository.PublicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final int MIN_PUBLICATIONS = 9;

    private final CourseRepository courseRepository;
    private final ProfessorRepository professorRepository;
    private final PublicationRepository publicationRepository;

    public ReportService(CourseRepository courseRepository, ProfessorRepository professorRepository, PublicationRepository publicationRepository) {
        this.courseRepository = courseRepository;
        this.professorRepository = professorRepository;
        this.publicationRepository = publicationRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseComplianceDto> getCourseCompliance() {
        List<Course> courses = courseRepository.findAll();
        List<Professor> approvedProfessors = professorRepository.findAll().stream()
                .filter(p -> p.getStatus() == Professor.Status.APPROVED)
                .collect(Collectors.toList());

        LocalDate threeYearsAgo = LocalDate.now().minusYears(3);

        List<CourseComplianceDto> reports = new ArrayList<>();

        for (Course course : courses) {
            List<Professor> courseProfessors = approvedProfessors.stream()
                    .filter(p -> p.getCourseIds().contains(course.getId()))
                    .collect(Collectors.toList());

            long totalApproved = courseProfessors.size();
            long totalCompliant = 0;

            for (Professor p : courseProfessors) {
                long count = publicationRepository.countValidatedSince(p.getId(), threeYearsAgo);
                if (count >= MIN_PUBLICATIONS) {
                    totalCompliant++;
                }
            }

            double percentage = 0.0;
            if (totalApproved > 0) {
                percentage = (double) totalCompliant / totalApproved * 100.0;
            }

            reports.add(new CourseComplianceDto(
                    course.getId(),
                    course.getName(),
                    course.getCode(),
                    totalApproved,
                    totalCompliant,
                    percentage
            ));
        }

        return reports;
    }
}
