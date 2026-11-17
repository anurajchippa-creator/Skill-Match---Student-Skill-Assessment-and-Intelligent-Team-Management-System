# Software Components

## 1. Technology Stack (exactly as specified)

| Layer                  | Technology              | Purpose                              |
|------------------------|-------------------------|--------------------------------------|
| OS                     | Windows 10 / 11         | Target runtime environment           |
| Language               | Java 17+                | Core development language            |
| IDE                    | IntelliJ / Eclipse / VS Code | Development environment          |
| Frontend               | Java Swing              | Desktop GUI client                   |
| Backend                | Spring Boot 3.x         | REST API + business logic            |
| Database               | MySQL 8.x               | Persistent storage                   |
| Connectivity           | JDBC + Spring Data JPA  | ORM & data access                    |
| Build                  | Maven                   | Dependency & build management        |
| Browser (optional)     | Chrome / Edge           | Viewing generated reports            |
| VCS (optional)         | Git / GitHub            | Source control                       |

## 2. Backend Components (`backend/`)

### Packages
- `com.skillmatch` – Main application entry
- `config` – (ready for security / CORS config)
- `controller` – REST endpoints
  - `AuthController` – register / login
  - `ProjectController` – CRUD projects + recommendations
  - `StudentController` – student skills
- `model` – JPA entities (User, Skill, Project, StudentSkill, …)
- `repository` – Spring Data JPA interfaces
- `service` – Business logic
  - `AuthService` – registration & authentication (BCrypt)
  - `MatchingService` – core skill-matching algorithm
- `dto` – (reserved for request/response objects)

### Key Algorithm – MatchingService
```
For each student:
  For each required skill of the project:
    if student has the skill:
      levelScore = map(BEGINNER→0.4 … EXPERT→1.0)
      testComponent = test_score / 100
      contribution = 0.6 * levelScore + 0.4 * testComponent
  finalScore = average of contributions
Sort students by finalScore DESC → ranked recommendations
```

## 3. Frontend Components (`frontend/`)

| Class                | Responsibility                                      |
|----------------------|-----------------------------------------------------|
| `MainFrame`          | CardLayout container, application entry point       |
| `LoginPanel`         | Login / Register UI, calls `/api/auth`              |
| `Session`            | Holds logged-in user context                        |
| `StudentDashboard`   | Skills view, project list, journey overview         |
| `FacultyDashboard`   | Project management, create project, recommendations |

Communication: Java 11+ `HttpClient` + Gson JSON.

## 4. Database Components (`sql/`)

- `schema.sql` – Full DDL for all tables
- `seed.sql` – Demo users, skills, projects, sample scores

### Main Tables
users, student_profiles, skills, student_skills, skill_tests,  
projects, project_required_skills, teams, team_members, tasks, progress_logs

## 5. How the Two Journeys Map to Components

**Student Path**  
LoginPanel → StudentDashboard → (skills API) → MatchingService ranking → Team formation (future) → Dashboard progress

**Faculty Path**  
LoginPanel → FacultyDashboard → create Project → MatchingService.recommendations() → Approve team → Dashboard & Reports

## 6. Build & Run Commands

```bash
# Database
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/seed.sql

# Backend
cd backend
mvn clean spring-boot:run

# Frontend (new terminal)
cd frontend
mvn compile exec:java
```

Demo credentials (after seed):
- student1@college.edu / password123
- faculty1@college.edu / password123
