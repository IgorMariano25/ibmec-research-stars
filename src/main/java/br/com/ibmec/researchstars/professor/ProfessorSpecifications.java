package br.com.ibmec.researchstars.professor;

import org.springframework.data.jpa.domain.Specification;

public final class ProfessorSpecifications {

    private ProfessorSpecifications() {
    }

    public static Specification<Professor> byFilters(Professor.Status status, String q) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }

            if (q != null && !q.isBlank()) {
                var pattern = "%" + q.toLowerCase() + "%";
                var byName = cb.like(cb.lower(root.get("name")), pattern);
                var byEmail = cb.like(cb.lower(root.get("email")), pattern);
                var byLattes = cb.like(cb.lower(root.get("lattesUrl")), pattern);
                predicate = cb.and(predicate, cb.or(byName, byEmail, byLattes));
            }

            return predicate;
        };
    }
}
