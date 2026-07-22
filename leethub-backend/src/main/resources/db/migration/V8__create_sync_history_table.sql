CREATE TABLE sync_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    solution_id BIGINT NOT NULL,
    event_type ENUM('CREATED', 'UPDATED', 'AI_GENERATED', 'SYNCED', 'FAILED') NOT NULL,
    status ENUM('SUCCESS', 'FAILURE') NOT NULL,
    commit_sha VARCHAR(40),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (solution_id) REFERENCES solutions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sync_history_solution ON sync_history(solution_id);
CREATE INDEX idx_sync_history_created ON sync_history(created_at);
