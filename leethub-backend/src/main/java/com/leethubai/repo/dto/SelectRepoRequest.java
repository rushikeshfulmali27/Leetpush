package com.leethubai.repo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SelectRepoRequest {

    @NotBlank(message = "repoFullName must not be blank")
    private String repoFullName;

    private String branch = "main";
}
