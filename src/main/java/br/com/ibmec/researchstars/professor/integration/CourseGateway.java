package br.com.ibmec.researchstars.professor.integration;

import java.util.Set;

public interface CourseGateway {
    Set<Long> keepOnlyExistingCourseIds(Set<Long> courseIds);
}
