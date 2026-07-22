CREATE TABLE repositories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    github_repo_id VARCHAR(50) NOT NULL,
    repo_name VARCHAR(200) NOT NULL,
    repo_full_name VARCHAR(300) NOT NULL,
    default_branch VARCHAR(50) DEFAULT 'main',
    is_active BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_repo (user_id, github_repo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
