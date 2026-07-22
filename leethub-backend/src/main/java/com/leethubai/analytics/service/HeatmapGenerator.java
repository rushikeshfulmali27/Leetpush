package com.leethubai.analytics.service;

import com.leethubai.common.util.DateUtils;
import com.leethubai.sync.repository.SolutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates heatmap data (daily submission counts) for a given year.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HeatmapGenerator {

    private final SolutionRepository solutionRepository;

    public record HeatmapData(int year, List<Map<String, Object>> data, int maxCount) {}

    /**
     * Builds heatmap data for the given user and year.
     * Returns one entry per day in the year, with a count of submissions.
     */
    public HeatmapData generate(Long userId, int year) {
        LocalDate startDate = DateUtils.startOfYear(year);
        LocalDate endDate = DateUtils.endOfYear(year);

        List<Object> rawDates = solutionRepository.findSubmissionDatesByYear(
                userId,
                DateUtils.toStartOfDay(startDate),
                DateUtils.toStartOfDay(endDate.plusDays(1)));

        // Aggregate counts per date
        Map<String, Integer> countByDate = new HashMap<>();
        for (Object raw : rawDates) {
            try {
                String dateStr;
                if (raw instanceof java.sql.Date sqlDate) {
                    dateStr = sqlDate.toLocalDate().toString();
                } else {
                    dateStr = raw.toString();
                }
                countByDate.merge(dateStr, 1, Integer::sum);
            } catch (Exception e) {
                log.warn("Could not parse heatmap date: {}", raw);
            }
        }

        // Build complete list of all days in the year
        List<Map<String, Object>> data = new ArrayList<>();
        int maxCount = 0;
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            String dateStr = cursor.toString();
            int count = countByDate.getOrDefault(dateStr, 0);
            if (count > maxCount) maxCount = count;
            data.add(Map.of("date", dateStr, "count", count));
            cursor = cursor.plusDays(1);
        }

        return new HeatmapData(year, data, maxCount);
    }
}
