package br.com.ibmec.researchstars.professor.integration;

import java.util.List;

public interface ProfessorPublicationsGateway {
    List<Object> findPublicationsByProfessorId(Long professorId);
}
