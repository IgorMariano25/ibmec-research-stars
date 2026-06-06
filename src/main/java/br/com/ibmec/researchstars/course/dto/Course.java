package br.com.ibmec.researchstars.course.dto;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "COURSE")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}
