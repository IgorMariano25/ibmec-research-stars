package br.com.ibmec.researchstars.ranking.service;

import br.com.ibmec.researchstars.ranking.dto.MyRankingResponseDto;
import br.com.ibmec.researchstars.ranking.dto.RankingEntryDto;
import br.com.ibmec.researchstars.professor.Professor;
import br.com.ibmec.researchstars.professor.ProfessorRepository;
import br.com.ibmec.researchstars.publication.repository.PublicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankingService {

    private final ProfessorRepository professorRepository;
    private final PublicationRepository publicationRepository;

    public RankingService(ProfessorRepository professorRepository, PublicationRepository publicationRepository) {
        this.professorRepository = professorRepository;
        this.publicationRepository = publicationRepository;
    }

    @Transactional(readOnly = true)
    public Page<RankingEntryDto> getRanking(Pageable pageable) {
        List<RankingEntryDto> allEntries = buildRankingEntries();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allEntries.size());

        List<RankingEntryDto> pageContent;
        if (start <= end && start < allEntries.size()) {
            pageContent = allEntries.subList(start, end);
        } else {
            pageContent = new ArrayList<>();
        }

        return new PageImpl<>(pageContent, pageable, allEntries.size());
    }

    @Transactional(readOnly = true)
    public MyRankingResponseDto getMyRanking(Long professorId) {
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new IllegalArgumentException("Professor not found"));

        List<RankingEntryDto> allEntries = buildRankingEntries();

        for (int i = 0; i < allEntries.size(); i++) {
            if (allEntries.get(i).getProfessorId().equals(professor.getId())) {
                return new MyRankingResponseDto(i + 1, allEntries.get(i).getValidatedPublications());
            }
        }

        return new MyRankingResponseDto(0, 0);
    }

    private List<RankingEntryDto> buildRankingEntries() {
        LocalDate threeYearsAgo = LocalDate.now().minusYears(3);
        List<Professor> approvedProfessors = professorRepository.findAll().stream()
                .filter(p -> p.getStatus() == Professor.Status.APPROVED)
                .collect(Collectors.toList());

        List<RankingEntryDto> entries = new ArrayList<>();
        for (Professor p : approvedProfessors) {
            long count = publicationRepository.countValidatedSince(p.getId(), threeYearsAgo);
            entries.add(new RankingEntryDto(p.getId(), p.getName(), p.getLattesNumber(), count));
        }

        entries.sort(Comparator.comparing(RankingEntryDto::getValidatedPublications).reversed()
                .thenComparing(RankingEntryDto::getName));

        return entries;
    }
}
