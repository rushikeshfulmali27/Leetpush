package com.leethubai.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {
    private List<SearchHit> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String query;
    private Map<String, Object> appliedFilters;

    @Data
    @Builder
    public static class SearchHit {
        private Long id;
        private String title;
        private String difficulty;
        private List<String> tags;
        private String language;
        private String syncStatus;
        private Instant submittedAt;
    }
}
