package br.com.ibmec.researchstars.professor.integration;

import br.com.ibmec.researchstars.course.Course;
import br.com.ibmec.researchstars.course.CourseRepository;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class CourseGatewayImpl implements CourseGateway {

    private final CourseRepository courseRepository;

    public CourseGatewayImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public Set<Long> keepOnlyExistingCourseIds(Set<Long> courseIds) {
        return StreamSupport.stream(courseRepository.findAllById(courseIds).spliterator(), false)
                .map(Course::getId)
                .collect(Collectors.toSet());
    }
}
