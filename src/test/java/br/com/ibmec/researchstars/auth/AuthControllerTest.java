package br.com.ibmec.researchstars.auth;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ibmec.researchstars.auth.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new AuthExceptionHandler())
                .build();
    }

    @Test
    void registerReturnsFieldErrorWhenLattesUrlDoesNotUseHttpProtocol() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Ada Lovelace",
                      "email": "ada@ibmec.br",
                      "password": "secret123",
                      "lattesUrl": "ttp://lattes.cnpq.br/1111111111111111",
                      "courseIds": [1, 2]
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.fieldErrors", hasSize(1)))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("lattesUrl"));
    }

    @Test
    void registerAcceptsHttpLattesUrl() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Ada Lovelace",
                      "email": "ada@ibmec.br",
                      "password": "secret123",
                      "lattesUrl": "https://lattes.cnpq.br/1111111111111111",
                      "courseIds": [1, 2]
                    }
                    """))
            .andExpect(status().isCreated());

        verify(authService).register(any(RegisterRequest.class));
    }
}
