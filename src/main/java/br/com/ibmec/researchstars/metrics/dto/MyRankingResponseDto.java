package br.com.ibmec.researchstars.metrics.dto;

public class MyRankingResponseDto {

    private int position;
    private long validatedPublications;

    public MyRankingResponseDto() {
    }

    public MyRankingResponseDto(int position, long validatedPublications) {
        this.position = position;
        this.validatedPublications = validatedPublications;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getValidatedPublications() {
        return validatedPublications;
    }

    public void setValidatedPublications(long validatedPublications) {
        this.validatedPublications = validatedPublications;
    }
}
