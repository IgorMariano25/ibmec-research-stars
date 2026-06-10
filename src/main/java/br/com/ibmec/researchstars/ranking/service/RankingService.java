package br.com.ibmec.researchstars.ranking.service;

import br.com.ibmec.researchstars.ranking.dto.MyRankingResponseDto;
import br.com.ibmec.researchstars.ranking.dto.RankingEntryDto;
import br.com.ibmec.researchstars.professor.Professor;
import br.com.ibmec.researchstars.professor.ProfessorRepository;
import br.com.ibmec.researchstars.publication.repository.PublicationRepository;
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
    public List<RankingEntryDto> getRanking() {
        return buildRankingEntries();
    }

    @Transactional(readOnly = true)
    public MyRankingResponseDto getMyRanking(Long professorId) {
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new IllegalArgumentException("Professor not found"));

        List<RankingEntryDto> allEntries = buildRankingEntries();

        for (RankingEntryDto entry : allEntries) {
            if (entry.getProfessorId().equals(professor.getId())) {
                return new MyRankingResponseDto(entry.getRank(), entry.getValidatedPublicationsLast3Years());
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
            entries.add(new RankingEntryDto(p.getId(), p.getName(), p.getLattesUrl(), count));
        }

        entries.sort(Comparator.comparing(RankingEntryDto::getValidatedPublicationsLast3Years).reversed()
                .thenComparing(RankingEntryDto::getName));

        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }

        return entries;
    }
}
