package com.leethubai.analytics.service;

import com.leethubai.common.security.UserPrincipal;
import com.leethubai.sync.model.Difficulty;
import com.leethubai.sync.repository.SolutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final SolutionRepository solutionRepository;

    public Object getSummary() {
        log.info("Getting analytics summary");
        Long userId = getCurrentUserId();

        long totalSolved = solutionRepository.countByUserId(userId);
        long easySolved = solutionRepository.countByUserIdAndDifficulty(userId, Difficulty.EASY);
        long mediumSolved = solutionRepository.countByUserIdAndDifficulty(userId, Difficulty.MEDIUM);
        long hardSolved = solutionRepository.countByUserIdAndDifficulty(userId, Difficulty.HARD);

        // This week count
        Instant weekStart = LocalDate.now().with(DayOfWeek.MONDAY)
                .atStartOfDay(ZoneOffset.UTC).toInstant();
        long thisWeekSolved = solutionRepository.countByUserIdAndSubmittedAtAfter(userId, weekStart);

        // This month count
        Instant monthStart = LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(ZoneOffset.UTC).toInstant();
        long thisMonthSolved = solutionRepository.countByUserIdAndSubmittedAtAfter(userId, monthStart);

        // Streak calculation
        List<Object> rawDates = solutionRepository.findDistinctSubmissionDates(userId);
        int currentStreak = 0;
        int longestStreak = 0;

        if (!rawDates.isEmpty()) {
            List<LocalDate> dates = rawDates.stream()
                    .map(d -> {
                        if (d instanceof java.sql.Date) {
                            return ((java.sql.Date) d).toLocalDate();
                        }
                        return LocalDate.parse(d.toString());
                    })
                    .sorted(Comparator.reverseOrder())
                    .distinct()
                    .collect(Collectors.toList());

            // Current streak: consecutive days ending today or yesterday
            LocalDate today = LocalDate.now();
            if (!dates.isEmpty()) {
                LocalDate first = dates.get(0);
                if (first.equals(today) || first.equals(today.minusDays(1))) {
                    currentStreak = 1;
                    for (int i = 1; i < dates.size(); i++) {
                        if (dates.get(i).equals(dates.get(i - 1).minusDays(1))) {
                            currentStreak++;
                        } else {
                            break;
                        }
                    }
                }
            }

            // Longest streak
            if (!dates.isEmpty()) {
                List<LocalDate> sorted = dates.stream()
                        .sorted()
                        .collect(Collectors.toList());
                int streak = 1;
                longestStreak = 1;
                for (int i = 1; i < sorted.size(); i++) {
                    if (sorted.get(i).equals(sorted.get(i - 1).plusDays(1))) {
                        streak++;
                        longestStreak = Math.max(longestStreak, streak);
                    } else {
                        streak = 1;
                    }
                }
            }
        }

        return Map.of(
            "totalSolved", totalSolved,
            "easySolved", easySolved,
            "mediumSolved", mediumSolved,
            "hardSolved", hardSolved,
            "currentStreak", currentStreak,
            "longestStreak", longestStreak,
            "thisWeekSolved", thisWeekSolved,
            "thisMonthSolved", thisMonthSolved
        );
    }

    public Object getHeatmap(Integer year) {
        log.info("Getting heatmap for year: {}", year);
        Long userId = getCurrentUserId();

        int targetYear = year != null ? year : LocalDate.now().getYear();
        Instant yearStart = LocalDate.of(targetYear, 1, 1)
                .atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant yearEnd = LocalDate.of(targetYear, 12, 31)
                .atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        List<Object> rawDates = solutionRepository.findSubmissionDatesByYear(userId, yearStart, yearEnd);

        // Count submissions per date
        Map<String, Long> dateCounts = new HashMap<>();
        for (Object d : rawDates) {
            String dateStr;
            if (d instanceof java.sql.Date) {
                dateStr = ((java.sql.Date) d).toLocalDate().toString();
            } else {
                dateStr = d.toString();
            }
            dateCounts.merge(dateStr, 1L, Long::sum);
        }

        List<Map<String, Object>> data = dateCounts.entrySet().stream()
                .map(e -> Map.<String, Object>of("date", e.getKey(), "count", e.getValue()))
                .sorted(Comparator.comparing(m -> (String) m.get("date")))
                .collect(Collectors.toList());

        return Map.of("data", data);
    }

    public Object getLanguageDistribution() {
        log.info("Getting language distribution");
        Long userId = getCurrentUserId();

        List<Object[]> raw = solutionRepository.findLanguageDistribution(userId);
        long total = raw.stream().mapToLong(r -> (Long) r[1]).sum();

        List<Map<String, Object>> distribution = raw.stream()
                .map(r -> {
                    String language = (String) r[0];
                    long count = (Long) r[1];
                    double percentage = total > 0 ? Math.round(count * 1000.0 / total) / 10.0 : 0;
                    return Map.<String, Object>of(
                        "language", language,
                        "count", count,
                        "percentage", percentage
                    );
                })
                .collect(Collectors.toList());

        return Map.of("distribution", distribution);
    }

    public Object getTopicPerformance() {
        log.info("Getting topic performance");
        return Map.of("performance", List.of());
    }

    private Long getCurrentUserId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getId();
    }
}
