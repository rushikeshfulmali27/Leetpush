CREATE TABLE ai_explanations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    solution_id BIGINT NOT NULL UNIQUE,
    problem_summary TEXT,
    brute_force_approach TEXT,
    optimized_approach TEXT,
    time_complexity VARCHAR(50),
    space_complexity VARCHAR(50),
    patterns JSON,
    interview_notes TEXT,
    common_mistakes TEXT,
    revision_notes TEXT,
    ai_provider VARCHAR(20),
    ai_model VARCHAR(50),
    token_count INT,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (solution_id) REFERENCES solutions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
