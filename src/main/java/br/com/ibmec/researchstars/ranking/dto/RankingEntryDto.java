package br.com.ibmec.researchstars.ranking.dto;

public class RankingEntryDto {

    private int rank;
    private Long professorId;
    private String name;
    private String lattesNumber;
    private long validatedPublicationsLast3Years;

    public RankingEntryDto() {
    }

    public RankingEntryDto(Long professorId, String name, String lattesNumber, long validatedPublicationsLast3Years) {
        this.professorId = professorId;
        this.name = name;
        this.lattesNumber = lattesNumber;
        this.validatedPublicationsLast3Years = validatedPublicationsLast3Years;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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

    public long getValidatedPublicationsLast3Years() {
        return validatedPublicationsLast3Years;
    }

    public void setValidatedPublicationsLast3Years(long validatedPublicationsLast3Years) {
        this.validatedPublicationsLast3Years = validatedPublicationsLast3Years;
    }
}
