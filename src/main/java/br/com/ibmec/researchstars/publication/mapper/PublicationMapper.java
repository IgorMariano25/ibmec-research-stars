package br.com.ibmec.researchstars.publication.mapper;

import br.com.ibmec.researchstars.publication.Publication;
import br.com.ibmec.researchstars.publication.dto.PublicationResponse;
import org.springframework.stereotype.Component;

@Component
public class PublicationMapper {

    public PublicationResponse toResponse(Publication pub) {
        return new PublicationResponse(
                pub.getId(),
                pub.getTitle(),
                pub.getLink(),
                pub.getPublicationDate(),
                pub.getStatus(),
                pub.getProfessorId(),
                pub.getValidatedByUserId(),
                pub.getValidatedAt(),
                pub.getCreatedAt()
        );
    }
}
