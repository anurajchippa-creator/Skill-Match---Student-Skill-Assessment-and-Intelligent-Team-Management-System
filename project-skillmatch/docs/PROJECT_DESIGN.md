# Project Design Document
## ONE SYSTEM · TWO USER JOURNEYS – SkillMatch

### 1. System Goal
Build a unified platform that connects **Students** (who want to join projects) with **Faculty** (who create projects) through intelligent skill matching, team formation, gap analysis and continuous progress tracking.

### 2. High-Level Architecture

```
┌─────────────────────┐          REST / JSON          ┌──────────────────────┐
│   Java Swing UI     │  ◄──────────────────────────► │  Spring Boot Backend │
│  (Desktop Client)   │                               │  (Port 8080)         │
└─────────────────────┘                               └──────────┬───────────┘
                                                                 │
                                                                 │ JPA / JDBC
                                                                 ▼
                                                        ┌──────────────────┐
                                                        │     MySQL 8      │
                                                        └──────────────────┘
```

### 3. User Roles
| Role     | Capabilities |
|----------|--------------|
| Student  | Register, create profile, add skills, take skill tests, view scores, join recommended teams, track personal & project progress |
| Faculty  | Register, create projects, define required skills, set team size & tasks, approve teams, view team performance, generate reports |

### 4. Detailed Flow (matches the provided diagram)

**Common Start**
- START → Login / Register

**Student Branch**
- Create Profile
- Add Skills
- Take Skill Test
- Get Scores

**Faculty Branch**
- Create Project
- Required Skills
- Team Size / Tasks

**Convergence Point**
- Skill Matching
- Team Recommendation
- Skill Gap Analysis
- Faculty Approval
- Team Created

**Post-Team Formation**
- Project Dashboard
  - Left: Project Progress → Completed / Pending
  - Right: Skill Progress → Team Performance
- Reports
- END

### 5. Core Modules

1. **Authentication Module**  
   JWT-based or simple session for demo. Role-based access.

2. **Profile & Skills Module**  
   Students maintain skill inventory with proficiency levels (Beginner / Intermediate / Advanced / Expert).

3. **Skill Test Engine**  
   Multiple-choice or practical questions per skill. Auto-scoring.

4. **Project Module**  
   Faculty define title, description, required skills (with min level), team size, tasks, deadlines.

5. **Matching Engine**  
   Score = weighted average of skill overlap + skill-test scores + availability.  
   Generates ranked student lists per project.

6. **Team Formation & Approval**  
   Auto-recommend balanced teams → Faculty reviews & approves / rejects / requests changes.

7. **Dashboard & Progress**  
   Real-time task status, skill improvement tracking, individual contribution metrics.

8. **Reporting**  
   PDF / CSV export of team performance, skill gaps closed, project completion rate.

### 6. Database Schema (High Level)

- users (id, email, password_hash, role, full_name, created_at)
- student_profiles (user_id, bio, year, department, gpa)
- skills (id, name, category)
- student_skills (student_id, skill_id, level, test_score)
- skill_tests (id, skill_id, question, options, correct_answer)
- projects (id, faculty_id, title, description, team_size, status)
- project_required_skills (project_id, skill_id, min_level)
- teams (id, project_id, status, created_at)
- team_members (team_id, student_id, role_in_team)
- tasks (id, project_id, title, assigned_to, status, due_date)
- progress_logs (id, team_id, student_id, skill_id, note, timestamp)

### 7. Non-Functional Requirements
- Desktop-first (Swing) for lab environments
- Responsive API for future web/mobile clients
- Secure password storage (BCrypt)
- Clear separation of concerns (MVC + Service layer)
- Easy to demo offline after initial MySQL setup

### 8. Future Enhancements
- Web frontend (React / Angular)
- Real-time notifications
- AI-powered skill gap recommendations
- Integration with LMS (Moodle / Canvas)
