package br.com.ibmec.researchstars.professor.integration;

import br.com.ibmec.researchstars.auth.AppUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserDetails details) {
            return details.getUserId();
        }
        throw new IllegalStateException("Nenhum usuário autenticado no contexto");
    }
}
