CREATE DATABASE IF NOT EXISTS skillmatch;
USE skillmatch;

CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(120) NOT NULL,
    role            ENUM('STUDENT', 'FACULTY') NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS student_profiles (
    user_id         BIGINT PRIMARY KEY,
    department      VARCHAR(100),
    year_of_study   INT,
    bio             TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS skills (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(80) NOT NULL UNIQUE,
    category        VARCHAR(60)
);

CREATE TABLE IF NOT EXISTS student_skills (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT NOT NULL,
    skill_id        BIGINT NOT NULL,
    level           ENUM('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT') DEFAULT 'BEGINNER',
    test_score      DECIMAL(5,2) DEFAULT 0.00,
    UNIQUE KEY uk_student_skill (student_id, skill_id),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS projects (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_id      BIGINT NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    team_size       INT NOT NULL DEFAULT 3,
    status          ENUM('OPEN','MATCHING','IN_PROGRESS','COMPLETED','CANCELLED') DEFAULT 'OPEN',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (faculty_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS project_required_skills (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT NOT NULL,
    skill_id        BIGINT NOT NULL,
    min_level       ENUM('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT') DEFAULT 'INTERMEDIATE',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS teams (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT NOT NULL,
    status          ENUM('PROPOSED','APPROVED','REJECTED','ACTIVE','COMPLETED') DEFAULT 'PROPOSED',
    created_by      BIGINT,
    note            VARCHAR(500),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS team_members (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id         BIGINT NOT NULL,
    student_id      BIGINT NOT NULL,
    role_in_team    VARCHAR(60) DEFAULT 'MEMBER',
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tasks (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    assigned_to     BIGINT,
    status          ENUM('PENDING','IN_PROGRESS','COMPLETED') DEFAULT 'PENDING',
    due_date        DATE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
