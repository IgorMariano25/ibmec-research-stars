package br.com.ibmec.researchstars.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.ibmec.researchstars.auth.dto.RegisterRequest;
import br.com.ibmec.researchstars.course.Course;
import br.com.ibmec.researchstars.course.CourseRepository;
import br.com.ibmec.researchstars.professor.JwtService;
import br.com.ibmec.researchstars.professor.Professor;
import br.com.ibmec.researchstars.professor.ProfessorCourseChangeRequest;
import br.com.ibmec.researchstars.professor.ProfessorCourseChangeRequestRepository;
import br.com.ibmec.researchstars.professor.ProfessorRepository;
import br.com.ibmec.researchstars.user.User;
import br.com.ibmec.researchstars.user.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ProfessorCourseChangeRequestRepository courseChangeRequestRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(
                userRepository,
                professorRepository,
                courseRepository,
                courseChangeRequestRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void registerCreatesPendingCourseChangeRequestWithoutActiveCourses() {
        var request = new RegisterRequest(
                "Ada Lovelace",
                "ada@ibmec.br",
                "secret123",
                "https://lattes.cnpq.br/1111111111111111",
                "MAT-001",
                List.of(101L, 102L)
        );
        var softwareEngineering = buildCourse(101L, "Engenharia de Software", "ES01");
        var dataScience = buildCourse(102L, "Data Science", "DS01");
        when(userRepository.existsByEmail("ada@ibmec.br")).thenReturn(false);
        when(professorRepository.existsByLattesUrl("https://lattes.cnpq.br/1111111111111111"))
                .thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            var user = invocation.getArgument(0, User.class);
            setField(user, "id", 10L);
            return user;
        });
        when(courseRepository.findAllById(List.of(101L, 102L)))
                .thenReturn(List.of(softwareEngineering, dataScience));
        when(professorRepository.save(any(Professor.class))).thenAnswer(invocation -> {
            var professor = invocation.getArgument(0, Professor.class);
            setField(professor, "id", 1L);
            return professor;
        });
        when(jwtService.generateToken("ada@ibmec.br")).thenReturn("jwt");

        service.register(request);

        var professorCaptor = ArgumentCaptor.forClass(Professor.class);
        verify(professorRepository).save(professorCaptor.capture());
        assertThat(professorCaptor.getValue().getStatus()).isEqualTo(Professor.Status.PENDING);
        assertThat(professorCaptor.getValue().getCourseIds()).isEmpty();

        var requestCaptor = ArgumentCaptor.forClass(ProfessorCourseChangeRequest.class);
        verify(courseChangeRequestRepository).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getProfessorId()).isEqualTo(1L);
        assertThat(requestCaptor.getValue().getRequestedByUserId()).isEqualTo(10L);
        assertThat(requestCaptor.getValue().getRequestedCourseIds()).containsExactlyInAnyOrder(101L, 102L);
        assertThat(requestCaptor.getValue().getStatus()).isEqualTo(ProfessorCourseChangeRequest.Status.PENDING);
    }

    private Course buildCourse(Long id, String name, String code) {
        var course = new Course(name, code);
        setField(course, "id", id);
        return course;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
