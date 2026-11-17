# SkillMatch – Student Skill Assessment and Intelligent Team Management System

## 📌 Project Overview

SkillMatch is a Java-based application designed to assess students' technical skills and intelligently organize them into project teams based on their skill sets.

The system helps faculty members identify students' strengths, evaluate their technical abilities, manage projects, and create balanced teams based on project requirements.

It provides separate interfaces for students and faculty, making skill assessment and team management easier and more efficient.

## 🎯 Objectives

- Assess students' technical skills and proficiency levels.
- Manage student profiles and skill information.
- Create and manage academic projects.
- Match students' skills with project requirements.
- Recommend suitable team combinations.
- Manage teams, tasks, and project progress.
- Provide a centralized platform for faculty and students.

## ✨ Key Features

### Student Module
- Student registration and login.
- Student dashboard.
- Skill management.
- Skill assessment.
- View projects and team information.
- Track assigned tasks.

### Faculty Module
- Faculty login and dashboard.
- Student management.
- Project management.
- View student skills and assessment information.
- Intelligent team matching.
- Team and task management.

### Intelligent Team Management
- Analyze student skills.
- Compare skills with project requirements.
- Generate team recommendations.
- Support balanced project team formation.

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| Frontend | Java Swing |
| Backend | Java Spring Boot |
| Database | MySQL |
| API Communication | REST API |
| ORM | Spring Data JPA / Hibernate |
| Build Tool | Maven |
| Programming Language | Java 21 |
| Version Control | Git & GitHub |

## 🏗️ System Architecture

The project follows a three-layer architecture:

1. **Frontend:** Java Swing provides the graphical user interface for students and faculty.
2. **Backend:** Spring Boot handles business logic, authentication, project management, and REST API requests.
3. **Database:** MySQL stores student details, skills, projects, teams, and task information.

### Application Workflow

```text
       STUDENT / FACULTY
               |
               v
        JAVA SWING GUI
               |
               v
       REST API REQUEST
               |
               v
       SPRING BOOT BACKEND
               |
               v
       BUSINESS LOGIC
               |
               v
          MYSQL DATABASE
               |
               v
       API RESPONSE (JSON)
               |
               v
        JAVA SWING GUI
```

## 📂 Project Structure

```text
SkillMatch/
│
├── .gitignore
│
└── project-skillmatch/
    │
    ├── backend/
    │   ├── src/
    │   │   ├── main/
    │   │   │   ├── java/com/skillmatch/
    │   │   │   │   ├── config/
    │   │   │   │   ├── controller/
    │   │   │   │   ├── dto/
    │   │   │   │   ├── model/
    │   │   │   │   ├── repository/
    │   │   │   │   └── service/
    │   │   │   └── resources/
    │   │   └── test/
    │   └── pom.xml
    │
    ├── frontend/
    │   ├── src/
    │   │   └── main/java/com/skillmatch/ui/
    │   └── pom.xml
    │
    ├── database/
    ├── docs/
    ├── scripts/
    ├── sql/
    │   ├── schema.sql
    │   ├── seed.sql
    │   └── FIX_PROJECT_REQUIRED_SKILLS.sql
    │
    └── README.md
```

## ⚙️ Requirements

Before running the project, install:

- Java Development Kit (JDK 21).
- Apache Maven.
- MySQL Server.
- Visual Studio Code.
- Git.

## 🚀 Setup and Installation

### 1. Clone the Repository

```bash
git clone https://github.com/anurajchippa-creator/Skill-Match---Student-Skill-Assessment-and-Intelligent-Team-Management-System.git
```

### 2. Open the Project

Open the cloned repository in Visual Studio Code.

### 3. Configure MySQL

Start your MySQL server and execute the SQL scripts provided in the project's `sql` folder.

Configure your database connection in:

`backend/src/main/resources/application.properties`

Set your database password using the `DB_PASSWORD` environment variable.

For Windows PowerShell:

```powershell
$env:DB_PASSWORD="your_mysql_password"
```

### 4. Run the Backend

Open a terminal inside the backend folder:

```powershell
cd project-skillmatch/backend
mvn spring-boot:run
```

The Spring Boot backend uses port 8080 by default.

### 5. Run the Frontend

Open the frontend folder in VS Code and run the main Java Swing application from the Java editor.

Ensure that the Spring Boot backend and MySQL server are running before using the frontend.

## 🔐 Security

- Database credentials should not be committed to GitHub.
- Database passwords are configured through environment variables.
- Backend APIs handle communication between the frontend and database.

## 🔮 Future Enhancements

- Advanced skill-matching algorithms.
- AI-based team recommendations.
- Detailed student performance analytics.
- Automated report generation.
- Email notifications.
- Enhanced faculty dashboards.

## 👨‍💻 Developed By

**SkillMatch Project Team**

Academic Project – Student Skill Assessment and Intelligent Team Management System.

## 📄 License

This project is developed for academic and educational purposes.
