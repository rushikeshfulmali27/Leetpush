package com.leethubai.analytics.service;

import com.leethubai.common.util.DateUtils;
import com.leethubai.sync.repository.SolutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;

/**
 * Computes current and longest solving streaks from submission dates.
 * A "streak" is consecutive calendar days with at least one solution submitted.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreakCalculator {

    private final SolutionRepository solutionRepository;

    public record StreakResult(int currentStreak, int longestStreak) {}

    /**
     * Computes streak data for a user based on distinct submission dates.
     */
    public StreakResult calculate(Long userId) {
        List<Object> rawDates = solutionRepository.findDistinctSubmissionDates(userId);

        if (rawDates.isEmpty()) {
            return new StreakResult(0, 0);
        }

        // Parse and sort descending
        TreeSet<LocalDate> dates = new TreeSet<>();
        for (Object raw : rawDates) {
            try {
                if (raw instanceof java.sql.Date sqlDate) {
                    dates.add(sqlDate.toLocalDate());
                } else if (raw instanceof String s) {
                    dates.add(LocalDate.parse(s));
                } else if (raw instanceof LocalDate ld) {
                    dates.add(ld);
                }
            } catch (Exception e) {
                log.warn("Could not parse submission date: {}", raw);
            }
        }

        LocalDate today = DateUtils.todayUtc();
        LocalDate yesterday = today.minusDays(1);

        // Current streak: count backwards from today or yesterday
        int currentStreak = 0;
        LocalDate checkDate = dates.contains(today) ? today : yesterday;

        if (dates.contains(checkDate)) {
            currentStreak = 1;
            LocalDate prev = checkDate.minusDays(1);
            while (dates.contains(prev)) {
                currentStreak++;
                prev = prev.minusDays(1);
            }
        }

        // Longest streak: iterate all dates
        int longestStreak = 0;
        int runningStreak = 0;
        LocalDate prevDate = null;

        for (LocalDate date : dates) {
            if (prevDate == null || date.equals(prevDate.plusDays(1))) {
                runningStreak++;
            } else {
                longestStreak = Math.max(longestStreak, runningStreak);
                runningStreak = 1;
            }
            prevDate = date;
        }
        longestStreak = Math.max(longestStreak, runningStreak);

        return new StreakResult(currentStreak, longestStreak);
    }
}
