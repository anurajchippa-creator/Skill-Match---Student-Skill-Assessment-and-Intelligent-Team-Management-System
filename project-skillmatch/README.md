# SkillMatch – One System · Two User Journeys

Full-stack college project management system implementing the complete dual-journey architecture:
**Student Pathway + Faculty Pathway → Skill Matching → Team Recommendation → Skill Gap Analysis → Faculty Approval → Team Created → Project Dashboard (Project Progress + Team Performance) → Reports**.

## Authentication & Logins
- **All Logins Password**: `Password123` (case-insensitive fallback also enabled for smooth presentation).
- **AIML-D Students (64 Total)**:
  - Any student can log in using their **Roll Number** (e.g. `25R11A66L3`) or their **Email** (e.g. `25r11a66l3@college.edu`) with password: `Password123`.
  - Example: Chippa Anuraj (`25R11A66L3` / `Password123`).
- **Demo Logins**:
  - **Demo Student**: `student1@college.edu` / `Password123`
  - **Demo Faculty**: `faculty1@college.edu` / `Password123`
- **Any Custom Email Registration**:
  - Students and faculty can register with any personal/college email of choice.

## How to Run

1. Ensure MySQL is running.
2. In terminal 1 (Backend API):
   ```bash
   cd backend
   mvn spring-boot:run
   ```
3. In terminal 2 (Desktop UI):
   ```bash
   cd frontend
   mvn compile exec:java
   ```

## Complete Feature Matrix

### 🏛️ Faculty / Lecturer Journey
1. **My Projects**: View all projects with required skills, status, and assigned teams.
2. **Create Project**: Set Title, Description, Team Size, **Required Skills with Target Level**, and Work Modules (Tasks).
3. **Team Recommendations**: Match student candidates based on verified skill scores.
4. **Skill Gap Analysis**: Deep analysis of candidate or proposed teams against project requirements (Satisfied / Deficient / Missing).
5. **Team Approvals**: Review pending teams with 1-click Skill Gap check before approving/rejecting.
6. **Project Progress**: Track and update module % and status (Completed / In Progress / Pending).
7. **Team Performance**: Track Team Performance Index (0-100), task completion velocity, and skill progress.
8. **Course Questions (Post Questions)**:
   - Lecturers can post new multiple-choice questions (Question, Options A–D, Correct Answer) to any course / skill (or create new courses).
   - Preserves all pre-seeded questions (Java, Spring Boot, MySQL, React, Python, Git, Project Management, Machine Learning).
9. **Comprehensive Reports**: Exportable executive reports covering progress, skill gaps, and performance metrics.

### 🎓 Student Journey
1. **Student Profile**: Overview card with roll number, verified skills count, score average, and assigned project/team.
2. **Add Skills**: Select skills and initial self-assessment proficiency level.
3. **Take Skill Test & Get Scores**:
   - Answer interactive questions from the Course Question Bank (including newly posted questions from lecturers + preserved course questions).
   - Instant automated grading and score verification.
4. **Skill Matching**: Real-time matching against open projects with % match, satisfied skills, and missing skills.
5. **Propose Team**: Form teams with peers and submit proposals directly for faculty review.
6. **Skill Gap Analysis**: Inspect requirements vs student/team skillset with gap severity and recommendations.
7. **Project Progress**: Module-by-module % completion tracking for assigned projects.
8. **Team Performance**: View composite performance score, module-to-skill progress, and member mastery contributions.
