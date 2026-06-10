package br.com.ibmec.researchstars.publication.mapper;

import br.com.ibmec.researchstars.publication.Publication;
import br.com.ibmec.researchstars.publication.dto.PublicationResponse;
import org.springframework.stereotype.Component;

@Component
public class PublicationMapper {

    public PublicationResponse toResponse(Publication pub) {
        return toResponse(pub, null);
    }

    public PublicationResponse toResponse(Publication pub, String professorName) {
        return new PublicationResponse(
                pub.getId(),
                pub.getTitle(),
                pub.getLink(),
                pub.getPublicationDate(),
                pub.getPublicationType(),
                pub.getAbntReference(),
                pub.getStatus(),
                pub.getProfessorId(),
                professorName,
                pub.getValidatedByUserId(),
                pub.getValidatedAt(),
                pub.getCreatedAt()
        );
    }
}
