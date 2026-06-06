package br.com.ibmec.researchstars.course;

import br.com.ibmec.researchstars.common.exception.DuplicateResourceException;
import br.com.ibmec.researchstars.course.dto.CourseDto;
import br.com.ibmec.researchstars.course.dto.CreateCourseRequest;
import br.com.ibmec.researchstars.course.mapper.CourseMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    public CourseService(CourseRepository courseRepository, CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
    }

    @Transactional(readOnly = true)
    public List<CourseDto> listAll(String query) {
        if (StringUtils.hasText(query)) {
            return courseRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(query, query)
                    .stream().map(courseMapper::toDto).toList();
        }
        return courseRepository.findAll().stream().map(courseMapper::toDto).toList();
    }

    public CourseDto create(CreateCourseRequest request) {
        String code = request.code().trim();
        if (courseRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Código de curso já em uso: " + code);
        }
        return courseMapper.toDto(courseRepository.save(courseMapper.toEntity(request)));
    }

    public CourseDto update(Long id, CreateCourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado: " + id));

        String code = request.code().trim();
        if (courseRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new DuplicateResourceException("Código de curso já em uso: " + code);
        }

        course.setName(request.name().trim());
        course.setCode(code);
        return courseMapper.toDto(courseRepository.save(course));
    }

    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado: " + id);
        }
        courseRepository.deleteById(id);
    }
}
