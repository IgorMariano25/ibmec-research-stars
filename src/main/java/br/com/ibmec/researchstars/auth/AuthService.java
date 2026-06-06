package br.com.ibmec.researchstars.auth;

import br.com.ibmec.researchstars.auth.dto.AuthResponse;
import br.com.ibmec.researchstars.auth.dto.LoginRequest;
import br.com.ibmec.researchstars.auth.dto.RegisterRequest;
import br.com.ibmec.researchstars.course.Course;
import br.com.ibmec.researchstars.course.CourseRepository;
import br.com.ibmec.researchstars.professor.JwtService;
import br.com.ibmec.researchstars.professor.Professor;
import br.com.ibmec.researchstars.professor.ProfessorRepository;
import br.com.ibmec.researchstars.user.User;
import br.com.ibmec.researchstars.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProfessorRepository professorRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       ProfessorRepository professorRepository,
                       CourseRepository courseRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.professorRepository = professorRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email já cadastrado");
        }
        if (professorRepository.existsByLattesNumber(request.lattesNumber())) {
            throw new RuntimeException("Número Lattes já cadastrado");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(User.Role.PROFESSOR);
        userRepository.save(user);

        List<Course> courses = courseRepository.findAllById(request.cursos());
        if (courses.size() != request.cursos().size()) {
            throw new RuntimeException("Um ou mais cursos não encontrados");
        }
        Set<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toSet());

        Professor professor = new Professor();
        professor.setName(request.name());
        professor.setEmail(request.email());
        professor.setLattesNumber(request.lattesNumber());
        professor.setMatricula(request.matricula());
        professor.setUserId(user.getId());
        professor.setStatus(Professor.Status.PENDING);
        professor.setCourseIds(courseIds);
        professorRepository.save(professor);

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), professor.getName(),
                user.getRole().name(), professor.getStatus().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user.getEmail());

        if (user.getRole() == User.Role.ADMIN) {
            return new AuthResponse(token, user.getEmail(), "Admin", "ADMIN", null);
        }

        Professor professor = professorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        return new AuthResponse(token, user.getEmail(), professor.getName(),
                user.getRole().name(), professor.getStatus().name());
    }
}
