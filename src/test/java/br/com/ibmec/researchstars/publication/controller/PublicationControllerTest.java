package br.com.ibmec.researchstars.publication.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ibmec.researchstars.auth.AppUserDetails;
import br.com.ibmec.researchstars.publication.PublicationStatus;
import br.com.ibmec.researchstars.publication.PublicationType;
import br.com.ibmec.researchstars.publication.dto.CreatePublicationRequest;
import br.com.ibmec.researchstars.publication.dto.PublicationResponse;
import br.com.ibmec.researchstars.publication.service.PublicationService;
import br.com.ibmec.researchstars.user.User;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.MethodParameter;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PublicationControllerTest {

    @Mock
    private PublicationService publicationService;

    private MockMvc mockMvc;
    private AppUserDetails principal;

    @BeforeEach
    void setUp() {
        principal = new AppUserDetails(
                10L,
                20L,
                "professor@ibmec.br",
                "hash",
                User.Role.PROFESSOR
        );
        var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new PublicationController(publicationService))
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void createKeepsPublicationDateAsExactDateOnlyValue() throws Exception {
        var response = new PublicationResponse(
                1L,
                "Paper",
                "https://example.com/paper",
                LocalDate.of(2026, 6, 10),
                PublicationType.JOURNAL_ARTICLE,
                "SOUZA, T. Paper. Journal, 2026.",
                PublicationStatus.PENDING,
                20L,
                "Professor",
                null,
                null,
                null
        );
        when(publicationService.create(eq(20L), org.mockito.ArgumentMatchers.any(CreatePublicationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/publications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Paper",
                                  "link": "https://example.com/paper",
                                  "publicationDate": "2026-06-10",
                                  "publicationType": "JOURNAL_ARTICLE",
                                  "abntReference": "SOUZA, T. Paper. Journal, 2026."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicationDate").value("2026-06-10"));

        var captor = ArgumentCaptor.forClass(CreatePublicationRequest.class);
        verify(publicationService).create(eq(20L), captor.capture());
        assertThat(captor.getValue().publicationDate()).isEqualTo(LocalDate.of(2026, 6, 10));
    }

    private class TestAuthenticationPrincipalResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && parameter.getParameterType().equals(AppUserDetails.class);
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            return principal;
        }
    }
}
