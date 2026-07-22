package com.leethubai.repo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RepositoryResponse {
    private Long id;
    private String repoName;
    private String repoFullName;
    private String defaultBranch;
    private Boolean isActive;
    private Long solutionCount;
}
