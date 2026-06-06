package br.com.ibmec.researchstars.auth.dto;

import br.com.ibmec.researchstars.course.dto.Course;

import java.util.List;

public record RegisterRequest(
        String name,
        String email,
        String password,
        String lattesNumber,
        String matricula,
        List<Long> cursos
) {}
