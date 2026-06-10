package br.com.ibmec.researchstars.report.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ReportingWindowServiceTest {

    @Test
    void defaultWindowStartsOnFirstDayOfCurrentYearMinusThreeYearsAndEndsToday() {
        var clock = Clock.fixed(Instant.parse("2026-06-10T12:00:00Z"), ZoneOffset.UTC);
        var service = new ReportingWindowService(clock);

        var window = service.defaultWindow();

        assertThat(window.startDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(window.endDate()).isEqualTo(LocalDate.of(2026, 6, 10));
    }
}
