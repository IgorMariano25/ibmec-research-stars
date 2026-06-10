package br.com.ibmec.researchstars.report.dto;

public class ProfessorComplianceDto {

    private Long professorId;
    private String professorName;
    private long validatedPublications;
    private boolean compliant;

    public ProfessorComplianceDto() {
    }

    public ProfessorComplianceDto(
            Long professorId,
            String professorName,
            long validatedPublications,
            boolean compliant
    ) {
        this.professorId = professorId;
        this.professorName = professorName;
        this.validatedPublications = validatedPublications;
        this.compliant = compliant;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public String getProfessorName() {
        return professorName;
    }

    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }

    public long getValidatedPublications() {
        return validatedPublications;
    }

    public void setValidatedPublications(long validatedPublications) {
        this.validatedPublications = validatedPublications;
    }

    public boolean isCompliant() {
        return compliant;
    }

    public void setCompliant(boolean compliant) {
        this.compliant = compliant;
    }
}
