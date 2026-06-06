package br.com.ibmec.researchstars.auth;

import br.com.ibmec.researchstars.professor.Professor;
import br.com.ibmec.researchstars.professor.ProfessorRepository;
import br.com.ibmec.researchstars.user.User;
import br.com.ibmec.researchstars.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ProfessorRepository professorRepository;

    public AppUserDetailsService(UserRepository userRepository,
                                 ProfessorRepository professorRepository) {
        this.userRepository = userRepository;
        this.professorRepository = professorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        Long professorId = null;
        if (user.getRole() == User.Role.PROFESSOR) {
            professorId = professorRepository.findByUserId(user.getId())
                    .map(Professor::getId)
                    .orElse(null);
        }

        return new AppUserDetails(user.getId(), professorId, user.getEmail(),
                user.getPasswordHash(), user.getRole());
    }
}
