CREATE TABLE user_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    solution_id BIGINT,
    content TEXT NOT NULL,
    note_type ENUM('PERSONAL', 'MISTAKE', 'REVISION', 'INTERVIEW') DEFAULT 'PERSONAL',
    reminder_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (solution_id) REFERENCES solutions(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_notes_user ON user_notes(user_id);
CREATE INDEX idx_user_notes_solution ON user_notes(solution_id);
CREATE INDEX idx_user_notes_reminder ON user_notes(user_id, reminder_at);
