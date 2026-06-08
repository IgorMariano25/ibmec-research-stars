package br.com.ibmec.researchstars.metrics.controller;

import br.com.ibmec.researchstars.auth.AppUserDetails;
import br.com.ibmec.researchstars.metrics.dto.MyRankingResponseDto;
import br.com.ibmec.researchstars.metrics.dto.RankingEntryDto;
import br.com.ibmec.researchstars.metrics.service.RankingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rankings")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<RankingEntryDto>> getRanking(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<RankingEntryDto> ranking = rankingService.getRanking(pageable);
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<MyRankingResponseDto> getMyRanking(
            @AuthenticationPrincipal AppUserDetails principal
    ) {
        MyRankingResponseDto myRanking = rankingService.getMyRanking(principal.getProfessorId());
        return ResponseEntity.ok(myRanking);
    }
}
