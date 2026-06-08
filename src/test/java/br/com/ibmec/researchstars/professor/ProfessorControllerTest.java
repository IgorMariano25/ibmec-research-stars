package br.com.ibmec.researchstars.professor;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ibmec.researchstars.course.dto.CourseDto;
import br.com.ibmec.researchstars.professor.dto.PagedResponse;
import br.com.ibmec.researchstars.professor.dto.ProfessorApproveResponse;
import br.com.ibmec.researchstars.professor.dto.ProfessorDetailResponse;
import br.com.ibmec.researchstars.professor.dto.ProfessorListItemResponse;
import br.com.ibmec.researchstars.professor.dto.ProfessorPublicationsResponse;
import br.com.ibmec.researchstars.professor.dto.ProfessorUpdateRequest;
import br.com.ibmec.researchstars.professor.exception.ProfessorConflictException;
import br.com.ibmec.researchstars.professor.exception.ProfessorIntegrationException;
import br.com.ibmec.researchstars.professor.exception.ProfessorNotFoundException;
import br.com.ibmec.researchstars.professor.exception.ProfessorStateException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ProfessorControllerTest {

    @Mock
    private ProfessorService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProfessorController(service))
                .setControllerAdvice(new ProfessorExceptionHandler())
                .build();
    }

    @Test
    void listReturnsPagedProfessors() throws Exception {
        var item = new ProfessorListItemResponse(
                1L,
                "Ada Lovelace",
                "ada@ibmec.br",
                "LAT-001",
                "MAT-001",
                Professor.Status.PENDING,
                null);
        when(service.list(Professor.Status.PENDING, "ada", 1, 5))
                .thenReturn(new PagedResponse<>(List.of(item), 1, 5, 1, 1));

        mockMvc.perform(get("/api/v1/professors")
                .param("status", "PENDING")
                .param("q", "ada")
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Ada Lovelace"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getByIdReturnsProfessorDetails() throws Exception {
        when(service.findById(1L)).thenReturn(detailResponse());

        mockMvc.perform(get("/api/v1/professors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.name").value("Ada Lovelace"))
                .andExpect(jsonPath("$.courses", hasSize(2)));
    }

    @Test
    void getByIdReturnsNotFoundWhenProfessorDoesNotExist() throws Exception {
        when(service.findById(404L)).thenThrow(new ProfessorNotFoundException(404L));

        mockMvc.perform(get("/api/v1/professors/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Professor not found: 404"));
    }

    @Test
    void meReturnsCurrentProfessorDetails() throws Exception {
        when(service.findMe()).thenReturn(detailResponse());

        mockMvc.perform(get("/api/v1/professors/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ada@ibmec.br"));
    }

    @Test
    void approveReturnsApprovedProfessor() throws Exception {
        when(service.approve(1L)).thenReturn(new ProfessorApproveResponse(1L, Professor.Status.APPROVED));

        mockMvc.perform(post("/api/v1/professors/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approveReturnsBadRequestWhenProfessorIsAlreadyApproved() throws Exception {
        when(service.approve(1L)).thenThrow(new ProfessorStateException("Professor is already approved"));

        mockMvc.perform(post("/api/v1/professors/1/approve"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Professor is already approved"));
    }

    @Test
    void updateReturnsUpdatedProfessorDetails() throws Exception {
        when(service.update(eq(1L), any(ProfessorUpdateRequest.class))).thenReturn(detailResponse());

        mockMvc.perform(patch("/api/v1/professors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Ada Lovelace",
                          "email": "ada@ibmec.br",
                          "matricula": "MAT-001",
                          "lattesNumber": "LAT-001",
                          "courseIds": [101, 102]
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ada Lovelace"))
                .andExpect(jsonPath("$.lattesNumber").value("LAT-001"));

        verify(service).update(eq(1L), any(ProfessorUpdateRequest.class));
    }

    @Test
    void updateReturnsConflictWhenUniqueFieldsAreAlreadyInUse() throws Exception {
        when(service.update(eq(1L), any(ProfessorUpdateRequest.class)))
                .thenThrow(new ProfessorConflictException("Email already in use"));

        mockMvc.perform(patch("/api/v1/professors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Ada Lovelace",
                          "email": "ada@ibmec.br",
                          "matricula": "MAT-001",
                          "lattesNumber": "LAT-001",
                          "courseIds": [101, 102]
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/professors/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void deleteReturnsNotFoundWhenProfessorDoesNotExist() throws Exception {
        doThrow(new ProfessorNotFoundException(404L)).when(service).delete(404L);

        mockMvc.perform(delete("/api/v1/professors/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Professor not found: 404"));
    }

    @Test
    void publicationsReturnsProfessorPublications() throws Exception {
        when(service.findProfessorPublications(1L))
                .thenReturn(new ProfessorPublicationsResponse(1L, List.<Object>of(Map.of("title", "Paper A"))));

        mockMvc.perform(get("/api/v1/professors/1/publications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professorId").value(1))
                .andExpect(jsonPath("$.publications", hasSize(1)))
                .andExpect(jsonPath("$.publications[0].title").value("Paper A"));
    }

    @Test
    void publicationsReturnsBadGatewayWhenIntegrationFails() throws Exception {
        when(service.findProfessorPublications(1L))
                .thenThrow(new ProfessorIntegrationException("Error querying professor publications",
                        new RuntimeException()));

        mockMvc.perform(get("/api/v1/professors/1/publications"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Error querying professor publications"));
    }

    private ProfessorDetailResponse detailResponse() {
        return new ProfessorDetailResponse(
                1L,
                10L,
                "Ada Lovelace",
                "ada@ibmec.br",
                "LAT-001",
                "MAT-001",
                Professor.Status.APPROVED,
                List.of(new CourseDto(101L, "Curso A", "A"), new CourseDto(102L, "Curso B", "B")),
                null);
    }
}
