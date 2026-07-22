CREATE TABLE solutions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    repository_id BIGINT,
    leetcode_id VARCHAR(20) NOT NULL,
    title VARCHAR(300) NOT NULL,
    title_slug VARCHAR(300) NOT NULL,
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL,
    language VARCHAR(30) NOT NULL,
    code MEDIUMTEXT NOT NULL,
    runtime_ms INT,
    runtime_percentile VARCHAR(10),
    memory_kb INT,
    memory_percentile VARCHAR(10),
    commit_sha VARCHAR(40),
    github_path VARCHAR(500),
    sync_status ENUM('PENDING', 'SYNCING', 'SYNCED', 'FAILED') DEFAULT 'PENDING',
    submitted_at TIMESTAMP NOT NULL,
    synced_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (repository_id) REFERENCES repositories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_solutions_user_id ON solutions(user_id);
CREATE INDEX idx_solutions_difficulty ON solutions(user_id, difficulty);
CREATE INDEX idx_solutions_sync_status ON solutions(sync_status);
CREATE INDEX idx_solutions_submitted_at ON solutions(user_id, submitted_at);
CREATE UNIQUE INDEX idx_solutions_user_leetcode ON solutions(user_id, leetcode_id, language);
CREATE FULLTEXT INDEX idx_solutions_search ON solutions(title, title_slug);
