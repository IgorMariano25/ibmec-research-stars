package br.com.ibmec.researchstars.ranking.dto;

public class MyRankingResponseDto {

    private int rank;
    private long validatedPublicationsLast3Years;

    public MyRankingResponseDto() {
    }

    public MyRankingResponseDto(int rank, long validatedPublicationsLast3Years) {
        this.rank = rank;
        this.validatedPublicationsLast3Years = validatedPublicationsLast3Years;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public long getValidatedPublicationsLast3Years() {
        return validatedPublicationsLast3Years;
    }

    public void setValidatedPublicationsLast3Years(long validatedPublicationsLast3Years) {
        this.validatedPublicationsLast3Years = validatedPublicationsLast3Years;
    }
}
