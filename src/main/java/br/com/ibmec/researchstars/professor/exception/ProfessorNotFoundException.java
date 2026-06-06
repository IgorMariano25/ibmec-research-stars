package br.com.ibmec.researchstars.professor.exception;

public class ProfessorNotFoundException extends RuntimeException {
    public ProfessorNotFoundException(Long id) {
        super("Professor not found: " + id);
    }

    public ProfessorNotFoundException(String message) {
        super(message);
    }
}
