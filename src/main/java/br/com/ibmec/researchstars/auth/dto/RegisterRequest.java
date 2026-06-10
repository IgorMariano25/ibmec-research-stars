package br.com.ibmec.researchstars.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.hibernate.validator.constraints.URL;

public record RegisterRequest(
        @NotBlank
        @Size(max = 255)
        String name,
        @NotBlank
        @Email
        @Size(max = 255)
        String email,
        @NotBlank
        @Size(min = 6)
        String password,
        @NotBlank
        @URL
        @Size(max = 1000)
        String lattesUrl,
        String matricula,
        @NotEmpty
        List<Long> courseIds
) {}
