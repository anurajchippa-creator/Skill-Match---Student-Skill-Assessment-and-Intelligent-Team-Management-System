package com.skillmatch.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.List;
import com.google.gson.*;

public class StudentDashboard extends JPanel {

    private final MainFrame mainFrame;
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private static final String BASE = "http://localhost:8080/api";

    // Models & Fields
    private JLabel profileSummaryLabel;
    private DefaultTableModel profileSkillsModel;
    private DefaultTableModel allProjectsModel;

    private DefaultTableModel skillsModel;
    private JComboBox<String> skillCombo;
    private JComboBox<String> levelCombo;
    private JComboBox<String> testSkillCombo;
    private JLabel scoreResultLabel;

    private DefaultTableModel matchModel;
    private JTextField proposeProjectIdField;
    private JTextField proposeMembersField;
    private JTextArea proposeNoteArea;

    // Skill Gap Components
    private JTextField gapProjectIdField;
    private JLabel gapOverviewLabel;
    private DefaultTableModel gapTableModel;
    private JTextArea gapRecommendationsArea;

    // Leader / Member Progress
    private DefaultTableModel leaderProgressModel;
    private JLabel leaderSummaryLabel;
    private JTextField leaderPidField;
    private java.util.List<Long> leaderTaskIds = new java.util.ArrayList<>();

    // Skill Progress & Performance
    private JTextField perfProjectIdField;
    private JLabel perfSummaryLabel;
    private DefaultTableModel perfModulesModel;
    private DefaultTableModel perfMembersModel;

    // Pre-seeded Base Questions (Guaranteed to NEVER be removed)
    private static final Map<String, String[][]> QUESTIONS = new LinkedHashMap<>();
    static {
        QUESTIONS.put("Java", new String[][]{
                {"Which keyword is used to inherit a class in Java?", "implements", "extends", "inherits", "super", "B"},
                {"What is the default value of a boolean in Java?", "true", "false", "null", "0", "B"},
                {"Which of these is not a Java primitive type?", "int", "boolean", "String", "char", "C"}
        });
        QUESTIONS.put("Spring Boot", new String[][]{
                {"Which annotation marks a Spring Boot main class?", "@SpringBootApplication", "@EnableAutoConfiguration", "@Configuration", "@Component", "A"},
                {"What does @RestController combine?", "@Controller + @ResponseBody", "@Service + @Repository", "@Entity + @Table", "@Bean + @Component", "A"}
        });
        QUESTIONS.put("MySQL", new String[][]{
                {"Which SQL clause filters rows?", "ORDER BY", "GROUP BY", "WHERE", "HAVING", "C"},
                {"Which command creates a new table?", "MAKE TABLE", "CREATE TABLE", "NEW TABLE", "ADD TABLE", "B"}
        });
        QUESTIONS.put("React", new String[][]{
                {"What is used to pass data to a child component?", "state", "props", "context", "redux", "B"},
                {"Which hook is used for side effects?", "useState", "useEffect", "useContext", "useReducer", "B"}
        });
        QUESTIONS.put("Python", new String[][]{
                {"How do you create a list in Python?", "list = {}", "list = []", "list = ()", "list = <>", "B"},
                {"Which keyword defines a function?", "func", "def", "function", "lambda", "B"}
        });
        QUESTIONS.put("Git", new String[][]{
                {"Which command stages all changes?", "git add .", "git commit -a", "git stage", "git push", "A"},
                {"Which command creates a new branch?", "git branch new", "git checkout -b new", "git new branch", "Both A and B", "D"}
        });
        QUESTIONS.put("Project Management", new String[][]{
                {"What does Agile emphasize?", "Heavy documentation", "Iterative development", "Waterfall phases", "No testing", "B"},
                {"What is a common Agile ceremony?", "Daily Standup", "Annual Review", "Quarterly Audit", "None", "A"}
        });
        QUESTIONS.put("Machine Learning", new String[][]{
                {"What is supervised learning?", "Learning without labels", "Learning with labeled data", "Reinforcement only", "Unsupervised clustering", "B"},
                {"Which library is popular for ML in Python?", "NumPy", "Pandas", "Scikit-learn", "All of the above", "D"}
        });
    }

    public StudentDashboard(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        setBackground(new Color(245, 247, 250));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Student Journey · One System Two User Journeys");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 60, 120));
        JLabel userLbl = new JLabel();
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.add(title, BorderLayout.WEST);
        header.add(userLbl, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Big Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

        addBigTab(tabs, "My Profile", createProfilePanel());
        addBigTab(tabs, "My Skills", createMySkillsPanel());
        addBigTab(tabs, "Add Skills", createAddSkillsPanel());
        addBigTab(tabs, "Take Skill Test", createSkillTestPanel());
        addBigTab(tabs, "Skill Matching", createMatchingPanel());
        addBigTab(tabs, "Propose Team", createProposeTeamPanel());
        addBigTab(tabs, "Skill Gap Analysis", createSkillGapPanel());
        addBigTab(tabs, "Project Progress", createLeaderProgressPanel());
        addBigTab(tabs, "Team Performance", createPerformancePanel());
        addBigTab(tabs, "All Projects", createAllProjectsPanel());

        add(tabs, BorderLayout.CENTER);

        // Footer
        JButton logout = new JButton("Logout");
        logout.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logout.addActionListener(e -> {
            Session.clear();
            mainFrame.showScreen("LOGIN");
        });
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(logout);
        add(footer, BorderLayout.SOUTH);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                userLbl.setText("Welcome, " + Session.getFullName() + " (Student)");
                loadProfile();
                refreshSkills();
                loadTestCourses();
            }
        });
    }

    private void addBigTab(JTabbedPane tabs, String title, JPanel panel) {
        tabs.addTab(title, panel);
        int index = tabs.getTabCount() - 1;
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        tabs.setTabComponentAt(index, lbl);
    }

    // ==================== 1. CREATE / VIEW PROFILE ====================
    private JPanel createProfilePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);

        profileSummaryLabel = new JLabel("Loading profile...");
        profileSummaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        profileSummaryLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 225, 245), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        profileSummaryLabel.setBackground(Color.WHITE);
        profileSummaryLabel.setOpaque(true);
        p.add(profileSummaryLabel, BorderLayout.NORTH);

        profileSkillsModel = new DefaultTableModel(new String[]{"Skill", "Proficiency Level", "Verified Test Score", "Status"}, 0);
        JTable table = new JTable(profileSkillsModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh Profile");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshBtn.addActionListener(e -> loadProfile());
        bottom.add(refreshBtn);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    private void loadProfile() {
        profileSkillsModel.setRowCount(0);
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/students/" + Session.getUserId() + "/profile"))
                    .GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonObject obj = gson.fromJson(resp.body(), JsonObject.class);
                String name = obj.get("fullName").getAsString();
                String email = obj.get("email").getAsString();
                String rollNo = obj.has("rollNumber") ? obj.get("rollNumber").getAsString() : "—";
                int totalSkills = obj.get("totalSkills").getAsInt();
                long verified = obj.get("verifiedSkills").getAsLong();
                double avg = obj.get("averageTestScore").getAsDouble();
                String role = obj.get("teamRole").getAsString();
                String proj = obj.get("assignedProjectTitle").getAsString();

                profileSummaryLabel.setText(String.format(
                        "<html><b>Student:</b> %s &nbsp;|&nbsp; <b>Roll No:</b> %s &nbsp;|&nbsp; <b>Email:</b> %s &nbsp;|&nbsp; <b>ID:</b> %d<br>"
                                + "<b>Skills Added:</b> %d &nbsp;|&nbsp; <b>Verified Skills (Tested):</b> %d &nbsp;|&nbsp; "
                                + "<b>Average Score:</b> <font color='blue'>%.1f%%</font><br>"
                                + "<b>Assigned Project:</b> %s &nbsp;|&nbsp; <b>Team Role:</b> %s</html>",
                        name, rollNo, email, Session.getUserId(), totalSkills, verified, avg, proj, role));

                JsonArray skills = obj.getAsJsonArray("skills");
                if (skills != null) {
                    for (JsonElement el : skills) {
                        JsonObject s = el.getAsJsonObject();
                        String skName = s.has("skill") && !s.get("skill").isJsonNull()
                                ? s.getAsJsonObject("skill").get("name").getAsString() : "—";
                        String lvl = s.has("level") ? s.get("level").getAsString() : "—";
                        double sc = s.has("testScore") ? s.get("testScore").getAsDouble() : 0.0;
                        String st = sc >= 50.0 ? "Verified" : (sc > 0 ? "Needs Practice" : "Not Tested");
                        profileSkillsModel.addRow(new Object[]{skName, lvl, String.format("%.1f%%", sc), st});
                    }
                }
            }
        } catch (Exception ex) {
            profileSummaryLabel.setText("Could not load profile: " + ex.getMessage());
        }
    }

    // ==================== 2. MY SKILLS ====================
    private JPanel createMySkillsPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        skillsModel = new DefaultTableModel(new String[]{"Skill", "Level", "Test Score", "Status"}, 0);
        JTable table = new JTable(skillsModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        JButton refresh = new JButton("Refresh My Skills");
        refresh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refresh.addActionListener(e -> refreshSkills());
        bottom.add(refresh);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private void refreshSkills() {
        skillsModel.setRowCount(0);
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/students/" + Session.getUserId() + "/skills"))
                    .GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonArray arr = gson.fromJson(resp.body(), JsonArray.class);
            if (arr != null) {
                for (JsonElement el : arr) {
                    JsonObject o = el.getAsJsonObject();
                    String skillName = o.has("skill") && !o.get("skill").isJsonNull()
                            ? o.getAsJsonObject("skill").get("name").getAsString() : "—";
                    String level = o.has("level") ? o.get("level").getAsString() : "—";
                    double score = o.has("testScore") ? o.get("testScore").getAsDouble() : 0;
                    String status = score >= 50 ? "Passed" : (score > 0 ? "Under Review" : "Pending Test");
                    skillsModel.addRow(new Object[]{skillName, level, String.format("%.1f%%", score), status});
                }
            }
        } catch (Exception ignored) {}
    }

    // ==================== 3. ADD SKILLS ====================
    private JPanel createAddSkillsPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(25, 45, 25, 45));

        JLabel heading = new JLabel("Add a New Skill to Your Profile");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heading.setForeground(new Color(30, 60, 120));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(heading);
        p.add(Box.createVerticalStrut(18));

        p.add(new JLabel("Select Skill (or choose from list):"));
        skillCombo = new JComboBox<>(new String[]{
                "Java", "Spring Boot", "MySQL", "React", "Python",
                "Machine Learning", "Project Management", "Git"
        });
        skillCombo.setEditable(true); // allow typing any skill
        skillCombo.setMaximumSize(new Dimension(320, 30));
        skillCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(skillCombo);
        p.add(Box.createVerticalStrut(14));

        p.add(new JLabel("Your Current Level:"));
        levelCombo = new JComboBox<>(new String[]{"BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"});
        levelCombo.setMaximumSize(new Dimension(320, 30));
        levelCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(levelCombo);
        p.add(Box.createVerticalStrut(22));

        JButton addBtn = new JButton("Add Skill to Profile");
        addBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        addBtn.setBackground(new Color(40, 100, 200));
        addBtn.setForeground(Color.WHITE);
        addBtn.setOpaque(true);
        addBtn.setContentAreaFilled(true);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.addActionListener(e -> addSkill());
        p.add(addBtn);

        p.add(Box.createVerticalStrut(20));
        JLabel note = new JLabel("<html><i>After adding a skill, navigate to the <b>Take Skill Test</b> tab<br>to answer questions, prove proficiency, and get your verified score!</i></html>");
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(note);

        return p;
    }

    private void addSkill() {
        Object item = skillCombo.getSelectedItem();
        String skillName = item != null ? item.toString().trim() : "";
        String level = (String) levelCombo.getSelectedItem();

        if (skillName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select or type a skill name");
            return;
        }

        try {
            JsonObject skillObj = new JsonObject();
            skillObj.addProperty("name", skillName);

            JsonObject body = new JsonObject();
            body.add("skill", skillObj);
            body.addProperty("level", level);
            body.addProperty("testScore", 0);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/students/" + Session.getUserId() + "/skills"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                JOptionPane.showMessageDialog(this,
                        "Skill \"" + skillName + "\" added!\n\nNow go to 'Take Skill Test' tab to verify your score.");
                refreshSkills();
                loadProfile();
                loadTestCourses();
            } else {
                JOptionPane.showMessageDialog(this, "Skill added or already on file. Go to 'Take Skill Test' to test.");
                refreshSkills();
                loadProfile();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ==================== 4. TAKE SKILL TEST & GET SCORES (Dynamic + Preserved) ====================
    private JPanel createSkillTestPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        top.setOpaque(false);
        top.add(new JLabel("Select Course / Skill to Test:"));
        testSkillCombo = new JComboBox<>(QUESTIONS.keySet().toArray(new String[0]));
        testSkillCombo.setPreferredSize(new Dimension(200, 28));
        top.add(testSkillCombo);

        JButton startBtn = new JButton("Start Test");
        startBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        startBtn.setBackground(new Color(40, 100, 200));
        startBtn.setForeground(Color.WHITE);
        startBtn.setOpaque(true);
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        top.add(startBtn);

        JButton refreshCoursesBtn = new JButton("Refresh Courses");
        refreshCoursesBtn.addActionListener(e -> loadTestCourses());
        top.add(refreshCoursesBtn);

        p.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(BorderFactory.createTitledBorder("Test Questions (Includes Lecturer Questions + Course Question Bank)"));
        p.add(scroll, BorderLayout.CENTER);

        scoreResultLabel = new JLabel("Select a skill and click 'Start Test' above.");
        scoreResultLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        scoreResultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(scoreResultLabel, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> {
            center.removeAll();
            scoreResultLabel.setText("Answer all questions and click 'Submit Test & Get Score' below.");
            String skill = (String) testSkillCombo.getSelectedItem();
            if (skill == null) return;

            // Load questions: combine database questions with guaranteed base questions
            List<String[]> allQuestions = fetchQuestionsForCourse(skill);
            if (allQuestions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No questions found for " + skill);
                return;
            }

            java.util.List<ButtonGroup> groups = new ArrayList<>();
            java.util.List<String> correctAnswers = new ArrayList<>();

            for (int i = 0; i < allQuestions.size(); i++) {
                String[] q = allQuestions.get(i);
                JPanel qPanel = new JPanel();
                qPanel.setLayout(new BoxLayout(qPanel, BoxLayout.Y_AXIS));
                qPanel.setOpaque(false);
                qPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(6, 6, 6, 6),
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 230, 240))
                ));
                qPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel qLabel = new JLabel((i + 1) + ". " + q[0]);
                qLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                qPanel.add(qLabel);

                ButtonGroup bg = new ButtonGroup();
                groups.add(bg);
                correctAnswers.add(q[5]); // A/B/C/D

                String[] opts = {"A", "B", "C", "D"};
                for (int j = 0; j < 4; j++) {
                    JRadioButton rb = new JRadioButton(opts[j] + ". " + q[j + 1]);
                    rb.setActionCommand(opts[j]);
                    rb.setOpaque(false);
                    rb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    bg.add(rb);
                    qPanel.add(rb);
                }
                center.add(qPanel);
            }

            JButton submitBtn = new JButton("Submit Test & Get Score");
            submitBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            submitBtn.setBackground(new Color(60, 160, 90));
            submitBtn.setForeground(Color.WHITE);
            submitBtn.setOpaque(true);
            submitBtn.setContentAreaFilled(true);
            submitBtn.setBorderPainted(false);
            submitBtn.setFocusPainted(false);
            submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            submitBtn.addActionListener(ev -> {
                int correct = 0;
                for (int i = 0; i < groups.size(); i++) {
                    ButtonModel sel = groups.get(i).getSelection();
                    if (sel != null && sel.getActionCommand().equalsIgnoreCase(correctAnswers.get(i))) {
                        correct++;
                    }
                }
                double score = (correct * 100.0) / groups.size();
                scoreResultLabel.setText(String.format(
                        "Your Score for %s: %.1f%%  (%d / %d correct) · Score saved to your profile!",
                        skill, score, correct, groups.size()));
                saveTestScore(skill, score);
            });

            center.add(Box.createVerticalStrut(12));
            center.add(submitBtn);
            center.revalidate();
            center.repaint();
        });

        return p;
    }

    private void loadTestCourses() {
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE + "/questions/courses")).GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            Set<String> set = new LinkedHashSet<>(QUESTIONS.keySet());
            if (resp.statusCode() == 200) {
                JsonArray arr = gson.fromJson(resp.body(), JsonArray.class);
                if (arr != null) {
                    for (JsonElement el : arr) set.add(el.getAsString());
                }
            }
            if (testSkillCombo != null) {
                testSkillCombo.removeAllItems();
                for (String s : set) testSkillCombo.addItem(s);
            }
            if (skillCombo != null) {
                for (String s : set) {
                    boolean exists = false;
                    for (int i = 0; i < skillCombo.getItemCount(); i++) {
                        if (s.equalsIgnoreCase(skillCombo.getItemAt(i))) { exists = true; break; }
                    }
                    if (!exists) skillCombo.addItem(s);
                }
            }
        } catch (Exception ignored) {}
    }

    private List<String[]> fetchQuestionsForCourse(String course) {
        List<String[]> list = new ArrayList<>();
        Set<String> seenTexts = new HashSet<>();

        // First, check backend database for all questions for this course
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/questions/course/" + course))
                    .GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonArray arr = gson.fromJson(resp.body(), JsonArray.class);
                if (arr != null && arr.size() > 0) {
                    for (JsonElement el : arr) {
                        JsonObject q = el.getAsJsonObject();
                        String qText = q.get("questionText").getAsString();
                        seenTexts.add(qText.toLowerCase().trim());
                        list.add(new String[]{
                                qText,
                                q.get("optionA").getAsString(),
                                q.get("optionB").getAsString(),
                                q.get("optionC").getAsString(),
                                q.get("optionD").getAsString(),
                                q.get("correctAnswer").getAsString()
                        });
                    }
                }
            }
        } catch (Exception ignored) {}

        // Next, merge base questions so pre-seeded questions are NEVER removed
        String[][] base = QUESTIONS.get(course);
        if (base != null) {
            for (String[] q : base) {
                if (!seenTexts.contains(q[0].toLowerCase().trim())) {
                    list.add(q);
                    seenTexts.add(q[0].toLowerCase().trim());
                }
            }
        }

        return list;
    }

    private void saveTestScore(String skillName, double score) {
        try {
            JsonObject skillObj = new JsonObject();
            skillObj.addProperty("name", skillName);

            JsonObject body = new JsonObject();
            body.add("skill", skillObj);
            body.addProperty("level", score >= 80 ? "ADVANCED" : (score >= 50 ? "INTERMEDIATE" : "BEGINNER"));
            body.addProperty("testScore", score);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/students/" + Session.getUserId() + "/skills"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                    .build();
            client.send(req, HttpResponse.BodyHandlers.ofString());
            refreshSkills();
            loadProfile();
            JOptionPane.showMessageDialog(this,
                    String.format("Score %.1f%% saved for course \"%s\"!\nYour profile and matching rank are updated.", score, skillName));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Score calculated: " + ex.getMessage());
        }
    }

    // ==================== 5. SKILL MATCHING ====================
    private JPanel createMatchingPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);

        JLabel intro = new JLabel("<html><b>AI Skill Matching Engine:</b> Open projects matched against your verified skills. "
                + "Click 'Propose Team for Selected' to form a group.</html>");
        intro.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(intro, BorderLayout.NORTH);

        matchModel = new DefaultTableModel(
                new String[]{"Project ID", "Project Title", "Match %", "Req Skills Count", "Matched Skills", "Missing Skills", "Status"}, 0);
        JTable table = new JTable(matchModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        bottom.setOpaque(false);
        JButton refreshBtn = new JButton("Run Skill Matching");
        JButton proposeBtn = new JButton("Propose Team for Selected");
        JButton viewGapBtn = new JButton("View Skill Gap Analysis");

        proposeBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        proposeBtn.setBackground(new Color(40, 100, 200));
        proposeBtn.setForeground(Color.WHITE);
        proposeBtn.setOpaque(true);
        proposeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        viewGapBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        viewGapBtn.setBackground(new Color(230, 120, 20));
        viewGapBtn.setForeground(Color.WHITE);
        viewGapBtn.setOpaque(true);
        viewGapBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        bottom.add(refreshBtn);
        bottom.add(proposeBtn);
        bottom.add(viewGapBtn);
        p.add(bottom, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadSkillMatches());
        proposeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a matched project row first");
                return;
            }
            long pid = (Long) matchModel.getValueAt(row, 0);
            proposeProjectIdField.setText(String.valueOf(pid));
            JOptionPane.showMessageDialog(this, "Project ID #" + pid + " selected. Navigating to 'Propose Team' tab.");
        });

        viewGapBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a matched project row first");
                return;
            }
            long pid = (Long) matchModel.getValueAt(row, 0);
            gapProjectIdField.setText(String.valueOf(pid));
            runSkillGapAnalysis();
            JOptionPane.showMessageDialog(this, "Skill Gap Analysis loaded in the 'Skill Gap Analysis' tab for Project #" + pid);
        });

        return p;
    }

    private void loadSkillMatches() {
        matchModel.setRowCount(0);
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/projects/student/" + Session.getUserId() + "/matches"))
                    .GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                JOptionPane.showMessageDialog(this, "Error " + resp.statusCode() + ": " + resp.body());
                return;
            }
            JsonArray arr = gson.fromJson(resp.body(), JsonArray.class);
            if (arr != null) {
                for (JsonElement el : arr) {
                    JsonObject o = el.getAsJsonObject();
                    double pct = o.get("matchPercentage").getAsDouble();
                    int totalReq = o.get("totalRequiredSkills").getAsInt();

                    StringBuilder matchedStr = new StringBuilder();
                    if (o.has("matchedSkills") && o.get("matchedSkills").isJsonArray()) {
                        for (JsonElement m : o.getAsJsonArray("matchedSkills")) {
                            if (matchedStr.length() > 0) matchedStr.append(", ");
                            matchedStr.append(m.getAsString());
                        }
                    }

                    StringBuilder missingStr = new StringBuilder();
                    if (o.has("missingSkills") && o.get("missingSkills").isJsonArray()) {
                        for (JsonElement m : o.getAsJsonArray("missingSkills")) {
                            if (missingStr.length() > 0) missingStr.append(", ");
                            missingStr.append(m.getAsString());
                        }
                    }

                    String status = pct >= 80 ? "Strong Match" : (pct >= 50 ? "Moderate Match" : "Skill Gap Exists");

                    matchModel.addRow(new Object[]{
                            o.get("projectId").getAsLong(),
                            o.get("title").getAsString(),
                            String.format("%.1f%%", pct),
                            totalReq,
                            matchedStr.length() > 0 ? matchedStr.toString() : "None",
                            missingStr.length() > 0 ? missingStr.toString() : "None",
                            status
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error matching skills: " + ex.getMessage());
        }
    }

    // ==================== 6. PROPOSE TEAM ====================
    private JPanel createProposeTeamPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel h = new JLabel("Propose Your Own Team for a Project");
        h.setFont(new Font("Segoe UI", Font.BOLD, 17));
        h.setForeground(new Color(30, 60, 120));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(8));

        JLabel info = new JLabel("<html>Students form a group and submit for <b>faculty approval</b>.<br>"
                + "Faculty reviews team skills and approves or rejects the proposal.</html>");
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(info);
        p.add(Box.createVerticalStrut(16));

        p.add(new JLabel("Project ID (from Skill Matching or Projects):"));
        proposeProjectIdField = new JTextField(10);
        proposeProjectIdField.setMaximumSize(new Dimension(250, 28));
        proposeProjectIdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(proposeProjectIdField);
        p.add(Box.createVerticalStrut(10));

        p.add(new JLabel("Team Member Student IDs (comma separated, e.g. 2, 3):"));
        proposeMembersField = new JTextField(20);
        proposeMembersField.setMaximumSize(new Dimension(420, 28));
        proposeMembersField.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(proposeMembersField);
        p.add(Box.createVerticalStrut(10));

        p.add(new JLabel("Proposal Note to Faculty:"));
        proposeNoteArea = new JTextArea(3, 30);
        proposeNoteArea.setLineWrap(true);
        JScrollPane sp = new JScrollPane(proposeNoteArea);
        sp.setMaximumSize(new Dimension(420, 80));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sp);
        p.add(Box.createVerticalStrut(16));

        JButton submit = new JButton("Submit Team for Faculty Approval");
        submit.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        submit.setBackground(new Color(40, 100, 200));
        submit.setForeground(Color.WHITE);
        submit.setOpaque(true);
        submit.setContentAreaFilled(true);
        submit.setBorderPainted(false);
        submit.setFocusPainted(false);
        submit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        submit.setAlignmentX(Component.LEFT_ALIGNMENT);
        submit.addActionListener(e -> proposeTeam());
        p.add(submit);
        return p;
    }

    private void proposeTeam() {
        String pid = proposeProjectIdField.getText().trim();
        if (pid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a Project ID");
            return;
        }
        try {
            java.util.List<Long> memberIds = new java.util.ArrayList<>();
            memberIds.add(Session.getUserId());
            String raw = proposeMembersField.getText().trim();
            if (!raw.isEmpty()) {
                for (String part : raw.split(",")) {
                    part = part.trim();
                    if (!part.isEmpty()) memberIds.add(Long.parseLong(part));
                }
            }
            JsonObject body = new JsonObject();
            body.addProperty("projectId", Long.parseLong(pid));
            body.addProperty("createdBy", Session.getUserId());
            body.addProperty("note", proposeNoteArea.getText().trim());
            JsonArray arr = new JsonArray();
            for (Long id : memberIds) arr.add(id);
            body.add("memberIds", arr);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/teams"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                JOptionPane.showMessageDialog(this,
                        "Team proposed successfully!\nFaculty will see it under Team Approvals.\n\n" + resp.body());
                loadProfile();
            } else {
                JOptionPane.showMessageDialog(this, "Failed: " + resp.body());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ==================== 7. SKILL GAP ANALYSIS ====================
    private JPanel createSkillGapPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        top.setOpaque(false);
        top.add(new JLabel("Project ID:"));
        gapProjectIdField = new JTextField(6);
        top.add(gapProjectIdField);

        JButton analyzeBtn = new JButton("Run Skill Gap Analysis");
        analyzeBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        analyzeBtn.setBackground(new Color(230, 120, 20));
        analyzeBtn.setForeground(Color.WHITE);
        analyzeBtn.setOpaque(true);
        analyzeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        top.add(analyzeBtn);

        p.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);

        gapOverviewLabel = new JLabel("Enter Project ID to analyze required skills against your profile.");
        gapOverviewLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gapOverviewLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 6, 6));
        center.add(gapOverviewLabel, BorderLayout.NORTH);

        gapTableModel = new DefaultTableModel(
                new String[]{"Required Skill", "Required Level", "Status", "Your Level", "Score", "Gap Analysis Note"}, 0);
        JTable table = new JTable(gapTableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createTitledBorder("Recommendations to Bridge Gaps"));
        gapRecommendationsArea = new JTextArea(4, 40);
        gapRecommendationsArea.setEditable(false);
        gapRecommendationsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bottom.add(new JScrollPane(gapRecommendationsArea), BorderLayout.CENTER);
        center.add(bottom, BorderLayout.SOUTH);

        p.add(center, BorderLayout.CENTER);

        analyzeBtn.addActionListener(e -> runSkillGapAnalysis());
        return p;
    }

    private void runSkillGapAnalysis() {
        String pid = gapProjectIdField.getText().trim();
        if (pid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a Project ID");
            return;
        }

        String url = BASE + "/projects/" + pid + "/skill-gap?studentId=" + Session.getUserId();
        gapTableModel.setRowCount(0);
        gapRecommendationsArea.setText("");

        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                JOptionPane.showMessageDialog(this, "Error " + resp.statusCode() + ": " + resp.body());
                return;
            }
            JsonObject obj = gson.fromJson(resp.body(), JsonObject.class);

            String title = obj.get("projectTitle").getAsString();
            double cov = obj.get("coveragePercentage").getAsDouble();
            String gapStatus = obj.get("gapStatus").getAsString();
            int satisfied = obj.get("satisfiedCount").getAsInt();
            int deficient = obj.get("deficientCount").getAsInt();
            int missing = obj.get("missingCount").getAsInt();

            gapOverviewLabel.setText(String.format(
                    "<html><b>Project:</b> %s | <b>Skill Coverage:</b> %.1f%% (%s) | "
                            + "<font color='green'>Satisfied: %d</font> | "
                            + "<font color='orange'>Deficient: %d</font> | "
                            + "<font color='red'>Missing: %d</font></html>",
                    title, cov, gapStatus, satisfied, deficient, missing));

            JsonArray skills = obj.getAsJsonArray("skills");
            if (skills != null) {
                for (JsonElement el : skills) {
                    JsonObject s = el.getAsJsonObject();
                    gapTableModel.addRow(new Object[]{
                            s.get("skillName").getAsString(),
                            s.get("requiredLevel").getAsString(),
                            s.get("status").getAsString(),
                            s.get("bestStudentLevel").getAsString(),
                            String.format("%.1f%%", s.get("bestTestScore").getAsDouble()),
                            s.get("gapNote").getAsString()
                    });
                }
            }

            JsonArray recs = obj.getAsJsonArray("recommendations");
            if (recs != null) {
                StringBuilder sb = new StringBuilder();
                for (JsonElement r : recs) {
                    sb.append("• ").append(r.getAsString()).append("\n");
                }
                gapRecommendationsArea.setText(sb.toString());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ==================== 8. PROJECT PROGRESS ====================
    private JPanel createLeaderProgressPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel info = new JLabel("<html><b>Project Progress Dashboard:</b> Team leader / members track module progress % "
                + "(Frontend, Backend, Database, Testing, Reporting).</html>");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(info, BorderLayout.NORTH);

        leaderProgressModel = new DefaultTableModel(new String[]{"Module ID", "Module", "Progress %", "Status"}, 0);
        JTable table = new JTable(leaderProgressModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        bottom.setOpaque(false);
        bottom.add(new JLabel("Project ID:"));
        leaderPidField = new JTextField(6);
        bottom.add(leaderPidField);

        JButton load = new JButton("Load Modules");
        JButton update = new JButton("Update Selected %");
        update.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        update.setBackground(new Color(60, 160, 90));
        update.setForeground(Color.WHITE);
        update.setOpaque(true);
        update.setFont(new Font("Segoe UI", Font.BOLD, 13));

        bottom.add(load);
        bottom.add(update);
        leaderSummaryLabel = new JLabel(" ");
        bottom.add(leaderSummaryLabel);
        p.add(bottom, BorderLayout.SOUTH);

        load.addActionListener(e -> {
            leaderProgressModel.setRowCount(0);
            leaderTaskIds.clear();
            String pid = leaderPidField.getText().trim();
            if (pid.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter Project ID");
                return;
            }
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks/project/" + pid + "/progress"))
                        .GET().build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                JsonObject o = gson.fromJson(resp.body(), JsonObject.class);
                leaderSummaryLabel.setText("Overall: " + o.get("percentComplete").getAsDouble() + "%  |  "
                        + (o.has("assignedTo") ? o.get("assignedTo").getAsString() : ""));
                JsonArray tasks = o.getAsJsonArray("tasks");
                if (tasks != null) {
                    for (JsonElement el : tasks) {
                        JsonObject t = el.getAsJsonObject();
                        long id = Long.parseLong(t.get("id").getAsString());
                        leaderTaskIds.add(id);
                        int pct = t.has("progressPercent") ? t.get("progressPercent").getAsInt() : 0;
                        leaderProgressModel.addRow(new Object[]{id, t.get("task").getAsString(), pct + "%", t.get("status").getAsString()});
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        update.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= leaderTaskIds.size()) {
                JOptionPane.showMessageDialog(this, "Select a module row");
                return;
            }
            String input = JOptionPane.showInputDialog(this, "New progress % for this module (0-100):", "50");
            if (input == null) return;
            try {
                int pct = Integer.parseInt(input.trim());
                JsonObject body = new JsonObject();
                body.addProperty("progressPercent", pct);
                body.addProperty("studentId", Session.getUserId());
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks/" + leaderTaskIds.get(row) + "/progress"))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                        .build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    JOptionPane.showMessageDialog(this, "Progress updated!");
                    load.doClick();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed (must be team LEADER of assigned team): " + resp.body());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        return p;
    }

    // ==================== 9. TEAM PERFORMANCE ====================
    private JPanel createPerformancePanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        top.setOpaque(false);
        top.add(new JLabel("Project ID:"));
        perfProjectIdField = new JTextField(6);
        top.add(perfProjectIdField);

        JButton loadBtn = new JButton("Load Team Performance");
        loadBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        loadBtn.setBackground(new Color(40, 100, 200));
        loadBtn.setForeground(Color.WHITE);
        loadBtn.setOpaque(true);
        loadBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        top.add(loadBtn);
        p.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);

        perfSummaryLabel = new JLabel("Enter Project ID to view Team Performance Index & Skill Progress.");
        perfSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        perfSummaryLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        center.add(perfSummaryLabel, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.5);
        split.setOpaque(false);

        JPanel modPanel = new JPanel(new BorderLayout());
        modPanel.setOpaque(false);
        modPanel.setBorder(BorderFactory.createTitledBorder("Skill Progress Across Project Modules"));
        perfModulesModel = new DefaultTableModel(
                new String[]{"Module", "Associated Skill Area", "Progress %", "Status"}, 0);
        JTable modTable = new JTable(perfModulesModel);
        modTable.setRowHeight(28);
        modTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        modPanel.add(new JScrollPane(modTable), BorderLayout.CENTER);
        split.setTopComponent(modPanel);

        JPanel memPanel = new JPanel(new BorderLayout());
        memPanel.setOpaque(false);
        memPanel.setBorder(BorderFactory.createTitledBorder("Team Members & Skill Mastery Contributions"));
        perfMembersModel = new DefaultTableModel(
                new String[]{"Student ID", "Member Name", "Role", "Skills Count", "Skill Test Average", "Activity Status"}, 0);
        JTable memTable = new JTable(perfMembersModel);
        memTable.setRowHeight(28);
        memTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        memPanel.add(new JScrollPane(memTable), BorderLayout.CENTER);
        split.setBottomComponent(memPanel);

        center.add(split, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);

        loadBtn.addActionListener(e -> {
            String pid = perfProjectIdField.getText().trim();
            if (pid.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter Project ID");
                return;
            }
            perfModulesModel.setRowCount(0);
            perfMembersModel.setRowCount(0);
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/projects/" + pid + "/team-performance"))
                        .GET().build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() != 200) {
                    JOptionPane.showMessageDialog(this, "Error: " + resp.body());
                    return;
                }
                JsonObject obj = gson.fromJson(resp.body(), JsonObject.class);
                String title = obj.get("projectTitle").getAsString();
                String leader = obj.get("teamLeader").getAsString();
                double perfScore = obj.get("performanceScore").getAsDouble();
                String rating = obj.get("performanceRating").getAsString();
                double taskAvg = obj.get("overallProgressPercent").getAsDouble();
                double skillAvg = obj.get("teamSkillAverage").getAsDouble();

                perfSummaryLabel.setText(String.format(
                        "<html><b>Project:</b> %s &nbsp;|&nbsp; <b>Leader:</b> %s &nbsp;|&nbsp; "
                                + "<b>Performance Score:</b> <font color='blue'>%.1f / 100 (%s)</font> &nbsp;|&nbsp; "
                                + "<b>Tasks Progress:</b> %.1f%% &nbsp;|&nbsp; <b>Skill Mastery Avg:</b> %.1f%%</html>",
                        title, leader, perfScore, rating, taskAvg, skillAvg));

                JsonArray skillProg = obj.getAsJsonArray("skillProgress");
                if (skillProg != null) {
                    for (JsonElement el : skillProg) {
                        JsonObject sp = el.getAsJsonObject();
                        perfModulesModel.addRow(new Object[]{
                                sp.get("module").getAsString(),
                                sp.get("associatedSkill").getAsString(),
                                sp.get("progressPercent").getAsInt() + "%",
                                sp.get("status").getAsString()
                        });
                    }
                }

                JsonArray members = obj.getAsJsonArray("members");
                if (members != null) {
                    for (JsonElement el : members) {
                        JsonObject m = el.getAsJsonObject();
                        perfMembersModel.addRow(new Object[]{
                                m.get("studentId").getAsLong(),
                                m.get("name").getAsString(),
                                m.get("role").getAsString(),
                                m.get("skillsCount").getAsInt(),
                                String.format("%.1f%%", m.get("skillScoreAverage").getAsDouble()),
                                m.get("status").getAsString()
                        });
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        return p;
    }

    // ==================== 10. ALL PROJECTS (Faculty Posted) ====================
    private JPanel createAllProjectsPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Header label
        JLabel heading = new JLabel("<html><b style='font-size:15px;color:#1e3c78;'>All Projects Posted by Faculty</b></html>");
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        p.add(heading, BorderLayout.NORTH);

        // Table
        allProjectsModel = new DefaultTableModel(
                new String[]{"ID", "Title", "Description", "Team Size", "Required Skills", "Status", "Assigned Team"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(allProjectsModel);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // Wider columns for description and skills
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(160);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom: refresh button + status label
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bottom.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh Projects");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshBtn.setBackground(new Color(40, 100, 200));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setOpaque(true);
        refreshBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        JLabel statusLbl = new JLabel("");
        statusLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLbl.setForeground(new Color(80, 120, 60));

        refreshBtn.addActionListener(e -> {
            statusLbl.setText("Loading...");
            loadAllProjects(statusLbl);
        });
        bottom.add(refreshBtn);
        bottom.add(statusLbl);
        p.add(bottom, BorderLayout.SOUTH);

        // Auto-load when panel is shown
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadAllProjects(statusLbl);
            }
        });

        return p;
    }

    private void loadAllProjects(JLabel statusLbl) {
        allProjectsModel.setRowCount(0);
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/projects"))
                    .GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonArray arr = gson.fromJson(resp.body(), JsonArray.class);
                int count = 0;
                if (arr != null) {
                    for (JsonElement el : arr) {
                        JsonObject o = el.getAsJsonObject();
                        long id = o.has("id") ? o.get("id").getAsLong() : 0;
                        String title = o.has("title") ? o.get("title").getAsString() : "—";
                        String desc = o.has("description") ? o.get("description").getAsString() : "—";
                        int teamSize = o.has("teamSize") ? o.get("teamSize").getAsInt() : 0;
                        String status = o.has("status") ? o.get("status").getAsString() : "—";
                        String assignedTeam = o.has("assignedTeamLabel") ? o.get("assignedTeamLabel").getAsString() : "Not assigned";

                        // Build required skills string
                        StringBuilder skills = new StringBuilder();
                        if (o.has("requiredSkills") && !o.get("requiredSkills").isJsonNull()) {
                            JsonArray sk = o.getAsJsonArray("requiredSkills");
                            for (int i = 0; i < sk.size(); i++) {
                                if (i > 0) skills.append(", ");
                                skills.append(sk.get(i).getAsString());
                            }
                        }
                        if (skills.length() == 0) skills.append("None specified");

                        allProjectsModel.addRow(new Object[]{
                                id, title, desc, teamSize, skills.toString(), status, assignedTeam
                        });
                        count++;
                    }
                }
                if (statusLbl != null) statusLbl.setText(count + " project(s) loaded.");
            } else {
                if (statusLbl != null) statusLbl.setText("Failed to load projects (HTTP " + resp.statusCode() + ").");
            }
        } catch (Exception ex) {
            if (statusLbl != null) statusLbl.setText("Error: " + ex.getMessage());
        }
    }
}
