package br.com.ibmec.researchstars.publication.dto;

import br.com.ibmec.researchstars.publication.PublicationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

// RF-10, RF-11, RF-18
public record CreatePublicationRequest(

        @NotBlank(message = "O título é obrigatório")
        String title,

        @NotBlank(message = "O link é obrigatório")
        @URL(message = "O link deve ser uma URL válida")
        String link,

        @NotNull(message = "A data de publicação é obrigatória")
        @PastOrPresent(message = "A data de publicação não pode ser no futuro")
        LocalDate publicationDate,

        @NotNull(message = "O tipo da publicação é obrigatório")
        PublicationType publicationType,

        @NotBlank(message = "A referência ABNT é obrigatória")
        String abntReference
) {}
