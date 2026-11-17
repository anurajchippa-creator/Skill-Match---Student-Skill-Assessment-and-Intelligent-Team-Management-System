-- Run this once in MySQL to fix the outdated table structure
-- (the old table had no 'id' column)

USE skillmatch;

DROP TABLE IF EXISTS project_required_skills;

CREATE TABLE project_required_skills (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT NOT NULL,
    skill_id        BIGINT NOT NULL,
    min_level       ENUM('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT') DEFAULT 'INTERMEDIATE',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);
