package br.com.ibmec.researchstars.professor.integration;

import br.com.ibmec.researchstars.publication.repository.PublicationRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProfessorPublicationsGatewayImpl implements ProfessorPublicationsGateway {

    private final PublicationRepository publicationRepository;

    public ProfessorPublicationsGatewayImpl(PublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }

    @Override
    public List<Object> findPublicationsByProfessorId(Long professorId) {
        return publicationRepository.findAllByProfessorId(professorId)
                .stream()
                .map(p -> (Object) p)
                .toList();
    }
}
