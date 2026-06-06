package br.com.ibmec.researchstars.auth;

import br.com.ibmec.researchstars.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AppUserDetails implements UserDetails {

    private final Long userId;
    private final Long professorId; // null para ADMIN
    private final String email;
    private final String passwordHash;
    private final User.Role role;

    public AppUserDetails(Long userId, Long professorId, String email,
                          String passwordHash, User.Role role) {
        this.userId = userId;
        this.professorId = professorId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getUserId() { return userId; }
    public Long getProfessorId() { return professorId; }
    public User.Role getRole() { return role; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
