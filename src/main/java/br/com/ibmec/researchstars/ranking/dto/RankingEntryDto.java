package br.com.ibmec.researchstars.ranking.dto;

public class RankingEntryDto {

    private Long professorId;
    private String name;
    private String lattesNumber;
    private long validatedPublications;

    public RankingEntryDto() {
    }

    public RankingEntryDto(Long professorId, String name, String lattesNumber, long validatedPublications) {
        this.professorId = professorId;
        this.name = name;
        this.lattesNumber = lattesNumber;
        this.validatedPublications = validatedPublications;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLattesNumber() {
        return lattesNumber;
    }

    public void setLattesNumber(String lattesNumber) {
        this.lattesNumber = lattesNumber;
    }

    public long getValidatedPublications() {
        return validatedPublications;
    }

    public void setValidatedPublications(long validatedPublications) {
        this.validatedPublications = validatedPublications;
    }
}
