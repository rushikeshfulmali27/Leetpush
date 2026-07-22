package com.leethubai.search.dto;

import lombok.Data;

@Data
public class SearchRequest {
    private String q;
    private String difficulty;
    private String tags;
    private String pattern;
    private String language;
    private int page = 0;
    private int size = 20;
}
