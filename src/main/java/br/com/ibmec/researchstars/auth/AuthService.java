package br.com.ibmec.researchstars.auth;
import br.com.ibmec.researchstars.auth.dto.AuthResponse;
import br.com.ibmec.researchstars.auth.dto.LoginRequest;
import br.com.ibmec.researchstars.auth.dto.RegisterRequest;
import br.com.ibmec.researchstars.course.dto.Course;
import br.com.ibmec.researchstars.professor.JwtService;
import br.com.ibmec.researchstars.professor.Professor;
import br.com.ibmec.researchstars.professor.ProfessorRepository;
import br.com.ibmec.researchstars.user.UserRepository;
import br.com.ibmec.researchstars.user.dto.User;
import br.com.ibmec.researchstars.course.CourseRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ProfessorRepository professorRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

        Professor professor = new Professor();
        professor.setName(request.name());
        professor.setEmail(request.email());
        professor.setLattesNumber(request.lattesNumber());
        professor.setStatus(Professor.Status.PENDING);
        professor.setCourses(courses);
        professor.setUser(user);
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

        Professor professor = professorRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        if (professor.getStatus() == Professor.Status.PENDING) {
            throw new RuntimeException("Cadastro aguardando aprovação");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), professor.getName(),
                user.getRole().name(), professor.getStatus().name());
    }
}