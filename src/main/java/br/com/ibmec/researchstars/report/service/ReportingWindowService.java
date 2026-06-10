package br.com.ibmec.researchstars.report.service;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class ReportingWindowService {

    private final Clock clock;

    public ReportingWindowService() {
        this(Clock.systemDefaultZone());
    }

    ReportingWindowService(Clock clock) {
        this.clock = clock;
    }

    public ReportingWindow defaultWindow() {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = LocalDate.of(today.getYear() - 3, 1, 1);
        return new ReportingWindow(startDate, today);
    }
}
