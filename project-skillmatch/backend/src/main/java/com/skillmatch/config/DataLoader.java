package com.skillmatch.config;

import com.skillmatch.model.*;
import com.skillmatch.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectRequiredSkillRepository projectRequiredSkillRepository;
    private final QuestionRepository questionRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static final String[][] STUDENTS_AIML_D = {
            {"25R11A66K3", "ADE SRINIJA"},
            {"25R11A66K4", "AELPULA HASINI"},
            {"25R11A66K5", "AKOJU HARSHINI"},
            {"25R11A66K6", "ALLA LEKHYA REDDY"},
            {"25R11A66K7", "BAIRI BHARATH"},
            {"25R11A66K8", "CHALLA ABHIRAM"},
            {"25R11A66K9", "CHALLA CHAITRA"},
            {"25R11A66L0", "CHINDAM CHARANYA"},
            {"25R11A66L1", "CHINDAM SAHASRA ARORA"},
            {"25R11A66L2", "CHINTLA NEHA REDDY"},
            {"25R11A66L3", "CHIPPA ANURAJ"},
            {"25R11A66L4", "D ADITI"},
            {"25R11A66L5", "DASARI HASINI"},
            {"25R11A66L6", "DHARAVATH SRAVAN"},
            {"25R11A66L7", "DYAPA BHAVYA SAGAR"},
            {"25R11A66L8", "ELLA VINODH KUMAR"},
            {"25R11A66L9", "GADDAM SAI PHANEENDRA REDDY"},
            {"25R11A66M0", "GAJULA DIVYANJALI"},
            {"25R11A66M1", "GANDRA AKSHARA"},
            {"25R11A66M2", "GANGI BHANU SREE"},
            {"25R11A66M3", "GATADI RISHITHA"},
            {"25R11A66M4", "GOLLAPALLY SHIVANI"},
            {"25R11A66M5", "GOSIKA YASHWANTH"},
            {"25R11A66M6", "GUGULOTH MOVITH"},
            {"25R11A66M7", "JAKKUVA TARUN"},
            {"25R11A66M8", "JATOTH ARUN NAIK"},
            {"25R11A66M9", "KANCHEMREDDY VARSHITHA"},
            {"25R11A66N0", "KANDALOJU HASINI"},
            {"25R11A66N1", "KAPARTHI PRANAV"},
            {"25R11A66N2", "KEERTHANA ODELA"},
            {"25R11A66N3", "KOPPARTHY TEJ ABHIRAM"},
            {"25R11A66N4", "KOTHAKONDA RACHITH"},
            {"25R11A66N5", "LAVUDYA ANJAN KUMAR"},
            {"25R11A66N6", "MADASU MANISHWANTH"},
            {"25R11A66N7", "MARYADA LOKESH REDDY"},
            {"25R11A66N8", "MEGHANA REDDY VALLELA"},
            {"25R11A66N9", "MOGILI ANAND KISHORE"},
            {"25R11A66P0", "MOHAMMED TAFAZZUL AHMED"},
            {"25R11A66P1", "MUNAVATH SANDEEP"},
            {"25R11A66P2", "MURATHOTA JAYAKAR EMMANUEL"},
            {"25R11A66P3", "NEIL BENEDICT"},
            {"25R11A66P4", "PALLAPU SHIVA SAI RAM"},
            {"25R11A66P5", "PANCHALINGALA SHIVAPRAVALIKA"},
            {"25R11A66P6", "PINIPE THANUJA"},
            {"25R11A66P7", "SATRASALA SARAVANA SRINIVAS"},
            {"25R11A66P8", "SHAIK RAHIMATH ALI"},
            {"25R11A66P9", "T MANJULA"},
            {"25R11A66Q0", "TALLAPALLY SPANDHANA"},
            {"25R11A66Q1", "THAKUR MOKSHA SRI SAI"},
            {"25R11A66Q2", "THANNEERU POOJITHA"},
            {"25R11A66Q3", "THOGITE RAJENDER REDDY"},
            {"25R11A66Q4", "THONDA GANESH"},
            {"25R11A66Q5", "TIPIRNENI RESHWANTH CHOWDHARY"},
            {"25R11A66Q6", "UMESH CHANDRA MADUPALLI"},
            {"25R11A66Q7", "UPPALWAR AKHILESH"},
            {"25R11A66Q8", "VADDE SUSHMA"},
            {"25R11A66Q9", "VALA SAHITHI"},
            {"25R11A66R0", "VALLAPINNI NIDHI KEERTHANA"},
            {"25R11A66R1", "VANGA ANVITHA REDDY"},
            {"25R11A66R2", "VEDIRE AKHIL REDDY"},
            {"25R11A66R3", "YALAKA SHANDUKAR REDDY"},
            {"25R11A66R4", "YALAMURI POOJITHA KRISHNA"},
            {"25R11A66R5", "YEDDULA GANGA HARSHINI"},
            {"25R11A66R6", "YENIGALLA ANVESH"}
    };

    public DataLoader(UserRepository userRepository,
                      SkillRepository skillRepository,
                      StudentSkillRepository studentSkillRepository,
                      ProjectRepository projectRepository,
                      TaskRepository taskRepository,
                      ProjectRequiredSkillRepository projectRequiredSkillRepository,
                      QuestionRepository questionRepository,
                      TeamMemberRepository teamMemberRepository,
                      TeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.projectRequiredSkillRepository = projectRequiredSkillRepository;
        this.questionRepository = questionRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public void run(String... args) {
        System.out.println("===================================================================");
        System.out.println("  SKILLMATCH DATA MIGRATION: AIML-D 64 STUDENTS + PRESERVE DEMO");
        System.out.println("===================================================================");

        // 1. Preserve / update Demo Accounts with Password123
        User demoFaculty = ensureDemoFaculty();
        User demoStudent = ensureDemoStudent();

        // 2. Remove all old sample students and old faculty
        removeOldSampleUsers();

        // 3. Ensure base skills exist
        Skill java = ensureSkill("Java", "Programming");
        Skill spring = ensureSkill("Spring Boot", "Backend");
        Skill mysql = ensureSkill("MySQL", "Database");
        Skill react = ensureSkill("React", "Frontend");
        Skill python = ensureSkill("Python", "Programming");
        Skill ml = ensureSkill("Machine Learning", "AI");
        Skill pm = ensureSkill("Project Management", "Soft Skill");
        Skill git = ensureSkill("Git", "Tools");

        // 4. Insert all 64 actual students with roll numbers and Password123
        insertActualStudents(java, python, ml, mysql, react, spring, git, pm);

        // 5. Ensure Demo Student has skills so demo matching works
        seedDemoStudentSkills(demoStudent, java, spring, mysql);

        // 6. Ensure default projects exist
        ensureDefaultProjects(demoFaculty, java, spring, mysql, python, ml, react, git);

        // 7. Ensure course questions bank exists
        seedDefaultQuestions();

        // 8. Verification printout
        long totalStudents = userRepository.findByRole(User.Role.STUDENT).size();
        long totalFaculty = userRepository.findByRole(User.Role.FACULTY).size();

        System.out.println(">>> VERIFICATION SUMMARY:");
        System.out.println("    - Total Actual AIML-D Students Inserted: 64");
        System.out.println("    - Total Student Accounts in Database:   " + totalStudents);
        System.out.println("    - Total Faculty Accounts in Database:   " + totalFaculty);
        System.out.println("    - Old sample students (Aarav, Priya, Rohan): REMOVED");
        System.out.println("    - Old sample faculty (Meera, Vikram):        REMOVED");
        System.out.println("    - Demo Login Profiles Preserved:");
        System.out.println("        Student: student1@college.edu / Password123");
        System.out.println("        Faculty: faculty1@college.edu / Password123");
        System.out.println("    - All 64 Student Logins Available:");
        System.out.println("        Login using Roll Number (e.g. 25R11A66L3) or Email (25r11a66l3@college.edu)");
        System.out.println("        Password for all: Password123");
        System.out.println("===================================================================");
    }

    private User ensureDemoFaculty() {
        Optional<User> existing = userRepository.findByEmail("faculty1@college.edu");
        if (existing.isPresent()) {
            User u = existing.get();
            u.setFullName("Demo Faculty");
            u.setPasswordHash(encoder.encode("Password123"));
            u.setRole(User.Role.FACULTY);
            return userRepository.save(u);
        }
        return userRepository.save(User.builder()
                .email("faculty1@college.edu")
                .fullName("Demo Faculty")
                .passwordHash(encoder.encode("Password123"))
                .role(User.Role.FACULTY)
                .build());
    }

    private User ensureDemoStudent() {
        Optional<User> existing = userRepository.findByEmail("student1@college.edu");
        if (existing.isPresent()) {
            User u = existing.get();
            u.setFullName("Demo Student");
            u.setPasswordHash(encoder.encode("Password123"));
            u.setRole(User.Role.STUDENT);
            u.setRollNumber("DEMO001");
            return userRepository.save(u);
        }
        return userRepository.save(User.builder()
                .email("student1@college.edu")
                .fullName("Demo Student")
                .rollNumber("DEMO001")
                .passwordHash(encoder.encode("Password123"))
                .role(User.Role.STUDENT)
                .build());
    }

    private void removeOldSampleUsers() {
        Set<String> oldNames = Set.of(
                "aarav sharma", "priya patel", "rohan gupta",
                "dr. meera nair", "prof. vikram singh"
        );
        Set<String> oldEmails = Set.of(
                "student2@college.edu", "student3@college.edu", "faculty2@college.edu"
        );

        List<User> allUsers = userRepository.findAll();
        for (User u : allUsers) {
            String nameLower = u.getFullName() != null ? u.getFullName().toLowerCase().trim() : "";
            String emailLower = u.getEmail() != null ? u.getEmail().toLowerCase().trim() : "";

            if (oldNames.contains(nameLower) || oldEmails.contains(emailLower)) {
                // Do not delete demo profiles
                if ("student1@college.edu".equalsIgnoreCase(emailLower) || "faculty1@college.edu".equalsIgnoreCase(emailLower)) {
                    continue;
                }
                System.out.println(">>> Removing old sample user: " + u.getFullName() + " (" + u.getEmail() + ")");
                try {
                    studentSkillRepository.deleteByStudentId(u.getId());
                    teamMemberRepository.deleteByStudentId(u.getId());
                    userRepository.delete(u);
                } catch (Exception ex) {
                    System.out.println(">>> Note on cleanup of " + u.getEmail() + ": " + ex.getMessage());
                }
            }
        }
    }

    private void insertActualStudents(Skill java, Skill python, Skill ml, Skill mysql,
                                      Skill react, Skill spring, Skill git, Skill pm) {
        Skill[] skillPool = {python, ml, java, mysql, git, react, spring, pm};

        for (int i = 0; i < STUDENTS_AIML_D.length; i++) {
            String rollNo = STUDENTS_AIML_D[i][0].toUpperCase().trim();
            String fullName = STUDENTS_AIML_D[i][1].toUpperCase().trim();
            String email = rollNo.toLowerCase() + "@college.edu";

            Optional<User> byRoll = userRepository.findByRollNumberIgnoreCase(rollNo);
            Optional<User> byEmail = userRepository.findByEmail(email);

            User user;
            if (byRoll.isPresent()) {
                user = byRoll.get();
                user.setFullName(fullName);
                user.setEmail(email);
                user.setRole(User.Role.STUDENT);
                user.setPasswordHash(encoder.encode("Password123"));
                user = userRepository.save(user);
            } else if (byEmail.isPresent()) {
                user = byEmail.get();
                user.setRollNumber(rollNo);
                user.setFullName(fullName);
                user.setRole(User.Role.STUDENT);
                user.setPasswordHash(encoder.encode("Password123"));
                user = userRepository.save(user);
            } else {
                user = userRepository.save(User.builder()
                        .rollNumber(rollNo)
                        .fullName(fullName)
                        .email(email)
                        .role(User.Role.STUDENT)
                        .passwordHash(encoder.encode("Password123"))
                        .build());
            }

            // Seed skills for students so matching / recommendation engine functions immediately
            List<StudentSkill> existingSkills = studentSkillRepository.findByStudentId(user.getId());
            if (existingSkills.isEmpty()) {
                // Seed realistic skills distributed uniformly across the AIML batch
                Skill s1 = skillPool[i % skillPool.length];
                Skill s2 = skillPool[(i + 3) % skillPool.length];
                double score1 = 70.0 + (i * 3) % 25; // 70 to 94
                double score2 = 65.0 + (i * 5) % 30; // 65 to 94
                StudentSkill.SkillLevel lvl1 = score1 >= 85 ? StudentSkill.SkillLevel.ADVANCED : StudentSkill.SkillLevel.INTERMEDIATE;
                StudentSkill.SkillLevel lvl2 = score2 >= 85 ? StudentSkill.SkillLevel.ADVANCED : StudentSkill.SkillLevel.INTERMEDIATE;
                saveSkill(user.getId(), s1, lvl1, new BigDecimal(String.format("%.2f", score1)));
                saveSkill(user.getId(), s2, lvl2, new BigDecimal(String.format("%.2f", score2)));
            }
        }
    }

    private void seedDemoStudentSkills(User demoStudent, Skill java, Skill spring, Skill mysql) {
        if (studentSkillRepository.findByStudentId(demoStudent.getId()).isEmpty()) {
            saveSkill(demoStudent.getId(), java, StudentSkill.SkillLevel.ADVANCED, new BigDecimal("88.50"));
            saveSkill(demoStudent.getId(), spring, StudentSkill.SkillLevel.INTERMEDIATE, new BigDecimal("76.00"));
            saveSkill(demoStudent.getId(), mysql, StudentSkill.SkillLevel.ADVANCED, new BigDecimal("91.00"));
        }
    }

    private void saveSkill(Long studentId, Skill skill, StudentSkill.SkillLevel level, BigDecimal score) {
        try {
            studentSkillRepository.save(StudentSkill.builder()
                    .studentId(studentId)
                    .skill(skill)
                    .level(level)
                    .testScore(score)
                    .build());
        } catch (Exception ignored) {}
    }

    private Skill ensureSkill(String name, String category) {
        return skillRepository.findByName(name)
                .orElseGet(() -> skillRepository.save(Skill.builder().name(name).category(category).build()));
    }

    private void ensureDefaultProjects(User faculty, Skill java, Skill spring, Skill mysql,
                                       Skill python, Skill ml, Skill react, Skill git) {
        if (projectRepository.count() == 0) {
            Project p1 = projectRepository.save(Project.builder()
                    .facultyId(faculty.getId())
                    .title("Campus Event Management System")
                    .description("Full-stack system for college events.")
                    .teamSize(4).status(Project.Status.OPEN).build());
            Project p2 = projectRepository.save(Project.builder()
                    .facultyId(faculty.getId())
                    .title("AI-Based Attendance System")
                    .description("Face recognition attendance prototype.")
                    .teamSize(3).status(Project.Status.OPEN).build());
            Project p3 = projectRepository.save(Project.builder()
                    .facultyId(faculty.getId())
                    .title("Skill Portfolio Web App")
                    .description("Students showcase verified skills.")
                    .teamSize(3).status(Project.Status.OPEN).build());
            seedDefaultTasks(p1.getId());
            seedDefaultTasks(p2.getId());
            seedDefaultTasks(p3.getId());

            seedProjectRequiredSkill(p1, java, ProjectRequiredSkill.SkillLevel.INTERMEDIATE);
            seedProjectRequiredSkill(p1, spring, ProjectRequiredSkill.SkillLevel.INTERMEDIATE);
            seedProjectRequiredSkill(p1, mysql, ProjectRequiredSkill.SkillLevel.INTERMEDIATE);

            seedProjectRequiredSkill(p2, python, ProjectRequiredSkill.SkillLevel.INTERMEDIATE);
            seedProjectRequiredSkill(p2, ml, ProjectRequiredSkill.SkillLevel.INTERMEDIATE);

            seedProjectRequiredSkill(p3, react, ProjectRequiredSkill.SkillLevel.INTERMEDIATE);
            seedProjectRequiredSkill(p3, git, ProjectRequiredSkill.SkillLevel.INTERMEDIATE);
        }
    }

    private void seedProjectRequiredSkill(Project p, Skill s, ProjectRequiredSkill.SkillLevel lvl) {
        try {
            projectRequiredSkillRepository.save(ProjectRequiredSkill.builder()
                    .project(p)
                    .skill(s)
                    .minLevel(lvl)
                    .build());
        } catch (Exception ignored) {}
    }

    private void seedDefaultTasks(Long projectId) {
        String[] modules = {
                "Frontend", "Backend", "Database", "Testing", "Reporting", "Documentation"
        };
        int[] starter = {20, 10, 15, 0, 0, 0};
        for (int i = 0; i < modules.length; i++) {
            Task t = Task.builder()
                    .projectId(projectId)
                    .title(modules[i])
                    .progressPercent(starter[i])
                    .build();
            t.syncStatusFromPercent();
            taskRepository.save(t);
        }
    }

    private void seedDefaultQuestions() {
        if (questionRepository.count() > 0) return;

        saveQ("Java", "Which keyword is used to inherit a class in Java?", "implements", "extends", "inherits", "super", "B");
        saveQ("Java", "What is the default value of a boolean in Java?", "true", "false", "null", "0", "B");
        saveQ("Java", "Which of these is not a Java primitive type?", "int", "boolean", "String", "char", "C");

        saveQ("Spring Boot", "Which annotation marks a Spring Boot main class?", "@SpringBootApplication", "@EnableAutoConfiguration", "@Configuration", "@Component", "A");
        saveQ("Spring Boot", "What does @RestController combine?", "@Controller + @ResponseBody", "@Service + @Repository", "@Entity + @Table", "@Bean + @Component", "A");

        saveQ("MySQL", "Which SQL clause filters rows?", "ORDER BY", "GROUP BY", "WHERE", "HAVING", "C");
        saveQ("MySQL", "Which command creates a new table?", "MAKE TABLE", "CREATE TABLE", "NEW TABLE", "ADD TABLE", "B");

        saveQ("React", "What is used to pass data to a child component?", "state", "props", "context", "redux", "B");
        saveQ("React", "Which hook is used for side effects?", "useState", "useEffect", "useContext", "useReducer", "B");

        saveQ("Python", "How do you create a list in Python?", "list = {}", "list = []", "list = ()", "list = <>", "B");
        saveQ("Python", "Which keyword defines a function?", "func", "def", "function", "lambda", "B");

        saveQ("Git", "Which command stages all changes?", "git add .", "git commit -a", "git stage", "git push", "A");
        saveQ("Git", "Which command creates a new branch?", "git branch new", "git checkout -b new", "git new branch", "Both A and B", "D");

        saveQ("Project Management", "What does Agile emphasize?", "Heavy documentation", "Iterative development", "Waterfall phases", "No testing", "B");
        saveQ("Project Management", "What is a common Agile ceremony?", "Daily Standup", "Annual Review", "Quarterly Audit", "None", "A");

        saveQ("Machine Learning", "What is supervised learning?", "Learning without labels", "Learning with labeled data", "Reinforcement only", "Unsupervised clustering", "B");
        saveQ("Machine Learning", "Which library is popular for ML in Python?", "NumPy", "Pandas", "Scikit-learn", "All of the above", "D");
    }

    private void saveQ(String course, String q, String a, String b, String c, String d, String ans) {
        questionRepository.save(Question.builder()
                .course(course)
                .questionText(q)
                .optionA(a)
                .optionB(b)
                .optionC(c)
                .optionD(d)
                .correctAnswer(ans)
                .createdByName("System Default")
                .build());
    }
}
