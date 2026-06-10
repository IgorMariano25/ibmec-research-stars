package br.com.ibmec.researchstars.report.service;

import java.time.LocalDate;

public record ReportingWindow(LocalDate startDate, LocalDate endDate) {
}
