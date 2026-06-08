package br.com.ibmec.researchstars.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.ibmec.researchstars.common.exception.DuplicateResourceException;
import br.com.ibmec.researchstars.course.dto.CourseDto;
import br.com.ibmec.researchstars.course.dto.CreateCourseRequest;
import br.com.ibmec.researchstars.course.mapper.CourseMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

/**
 * Testes de unidade do {@link CourseService}.
 *
 * <p>
 * Categoria: <b>Teste de Unidade</b> (executado pelo job Jenkins
 * "01-testes-de-unidade").
 * Usa Mockito para isolar o serviço das suas dependências (repositório e
 * mapper).
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    private CourseService service;

    @BeforeEach
    void setUp() {
        service = new CourseService(courseRepository, new CourseMapper());
    }

    @Test
    void listAllWithoutQueryReturnsAllCourses() {
        when(courseRepository.findAll()).thenReturn(List.of(course(1L, "Engenharia", "ENG")));

        List<CourseDto> result = service.listAll(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("ENG");
    }

    @Test
    void listAllWithQueryFiltersByNameOrCode() {
        when(courseRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase("eng", "eng"))
                .thenReturn(List.of(course(1L, "Engenharia", "ENG")));

        List<CourseDto> result = service.listAll("eng");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Engenharia");
    }

    @Test
    void createPersistsAndReturnsCourse() {
        when(courseRepository.existsByCodeIgnoreCase("ENG")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenReturn(course(10L, "Engenharia", "ENG"));

        CourseDto result = service.create(new CreateCourseRequest("Engenharia", "ENG"));

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.code()).isEqualTo("ENG");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createRejectsDuplicatedCode() {
        when(courseRepository.existsByCodeIgnoreCase("ENG")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateCourseRequest("Engenharia", "ENG")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ENG");

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateThrowsWhenCourseDoesNotExist() {
        when(courseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(404L, new CreateCourseRequest("Novo", "NEW")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void deleteThrowsWhenCourseDoesNotExist() {
        when(courseRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(404L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");

        verify(courseRepository, never()).deleteById(any());
    }

    private static Course course(Long id, String name, String code) {
        Course course = new Course(name, code);
        try {
            var field = Course.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(course, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return course;
    }
}
