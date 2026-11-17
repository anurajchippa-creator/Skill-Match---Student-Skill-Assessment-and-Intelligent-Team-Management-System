package com.skillmatch.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.List;
import com.google.gson.*;

public class FacultyDashboard extends JPanel {

    private final MainFrame mainFrame;
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private static final String BASE = "http://localhost:8080/api";

    private DefaultTableModel projectsModel;
    private DefaultTableModel teamsModel;
    private DefaultTableModel progressModel;
    private JLabel progressSummaryLabel;
    private JTextField progressProjectIdField;
    private java.util.List<Long> progressTaskIds = new java.util.ArrayList<>();

    // Skill Gap Components
    private JTextField gapProjectIdField;
    private JTextField gapTeamIdField;
    private JLabel gapOverviewLabel;
    private DefaultTableModel gapTableModel;
    private JTextArea gapRecommendationsArea;

    // Team Performance Components
    private JTextField perfProjectIdField;
    private JLabel perfSummaryLabel;
    private DefaultTableModel perfModulesModel;
    private DefaultTableModel perfMembersModel;

    // Course Questions Components
    private JComboBox<String> questionCourseCombo;
    private JTextField customCourseField;
    private JTextArea questionTextArea;
    private JTextField optAField, optBField, optCField, optDField;
    private JComboBox<String> correctAnsCombo;
    private DefaultTableModel questionsTableModel;
    private JComboBox<String> filterCourseCombo;

    public FacultyDashboard(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        setBackground(new Color(245, 247, 250));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Faculty Journey · One System Two User Journeys");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 60, 120));
        JLabel userLbl = new JLabel();
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.add(title, BorderLayout.WEST);
        header.add(userLbl, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

        addBigTab(tabs, "My Projects", createProjectsPanel());
        addBigTab(tabs, "Create Project", createCreatePanel());
        addBigTab(tabs, "Team Recommendations", createRecoPanel());
        addBigTab(tabs, "Skill Gap Analysis", createSkillGapPanel());
        addBigTab(tabs, "Team Approvals", createTeamApprovalsPanel());
        addBigTab(tabs, "Project Progress", createProgressPanel());
        addBigTab(tabs, "Team Performance", createPerformancePanel());
        addBigTab(tabs, "Course Questions", createPostQuestionsPanel());
        addBigTab(tabs, "Reports", createReportsPanel());

        add(tabs, BorderLayout.CENTER);

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
                userLbl.setText("Welcome, " + Session.getFullName() + " (Faculty)");
                loadProjects();
                loadCourses();
                loadCourseQuestions("ALL");
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

    // ==================== 1. MY PROJECTS ====================
    private JPanel createProjectsPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        projectsModel = new DefaultTableModel(
                new String[]{"ID", "Title", "Team Size", "Status", "Assigned To", "Required Skills"}, 0);
        JTable table = new JTable(projectsModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        JButton loadAll = new JButton("Refresh Projects List");
        loadAll.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loadAll.addActionListener(e -> loadProjects());
        bottom.add(loadAll);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private void loadProjects() {
        projectsModel.setRowCount(0);
        try {
            HttpResponse<String> resp = get(BASE + "/projects");
            if (resp.statusCode() != 200) {
                return;
            }
            JsonArray arr = gson.fromJson(resp.body(), JsonArray.class);
            if (arr == null || arr.size() == 0) return;

            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                String assigned = o.has("assignedTo") && !o.get("assignedTo").isJsonNull()
                        ? o.get("assignedTo").getAsString() : "Not assigned";

                StringBuilder reqSkills = new StringBuilder();
                if (o.has("requiredSkills") && o.get("requiredSkills").isJsonArray()) {
                    for (JsonElement sk : o.getAsJsonArray("requiredSkills")) {
                        if (reqSkills.length() > 0) reqSkills.append(", ");
                        reqSkills.append(sk.getAsString());
                    }
                }
                if (reqSkills.length() == 0) reqSkills.append("General");

                projectsModel.addRow(new Object[]{
                        o.get("id").getAsLong(),
                        o.get("title").getAsString(),
                        o.get("teamSize").getAsInt(),
                        o.get("status").getAsString(),
                        assigned,
                        reqSkills.toString()
                });
            }
        } catch (Exception ignored) {}
    }

    // ==================== 2. CREATE PROJECT ====================
    private JPanel createCreatePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        JTextField titleF = new JTextField(25);
        JTextArea descA = new JTextArea(3, 25);
        JSpinner sizeSpin = new JSpinner(new SpinnerNumberModel(3, 2, 10, 1));

        String[] availableSkills = {"Java", "Spring Boot", "MySQL", "React", "Python", "Machine Learning", "Git", "Project Management"};
        Map<String, JCheckBox> skillBoxes = new LinkedHashMap<>();
        JPanel skillsPanel = new JPanel(new GridLayout(2, 4, 8, 6));
        skillsPanel.setOpaque(false);
        for (String s : availableSkills) {
            JCheckBox cb = new JCheckBox(s);
            cb.setOpaque(false);
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            skillBoxes.put(s, cb);
            skillsPanel.add(cb);
        }

        JTextArea modulesA = new JTextArea(5, 25);
        modulesA.setText("Frontend\nBackend\nDatabase\nTesting\nReporting\nDocumentation");
        modulesA.setLineWrap(true);

        JLabel h = new JLabel("Create Project · Define Required Skills & Work Modules");
        h.setFont(new Font("Segoe UI", Font.BOLD, 16));
        h.setForeground(new Color(30, 60, 120));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(12));

        p.add(new JLabel("Project Title:"));
        titleF.setMaximumSize(new Dimension(500, 28));
        titleF.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(titleF);
        p.add(Box.createVerticalStrut(8));

        p.add(new JLabel("Description:"));
        JScrollPane descScroll = new JScrollPane(descA);
        descScroll.setMaximumSize(new Dimension(500, 65));
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(descScroll);
        p.add(Box.createVerticalStrut(8));

        p.add(new JLabel("Team Size:"));
        sizeSpin.setMaximumSize(new Dimension(100, 28));
        sizeSpin.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sizeSpin);
        p.add(Box.createVerticalStrut(10));

        p.add(new JLabel("Required Skills (Select skills needed for this project):"));
        skillsPanel.setMaximumSize(new Dimension(650, 60));
        skillsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(skillsPanel);
        p.add(Box.createVerticalStrut(10));

        p.add(new JLabel("Work Modules (Tasks to track in Project Progress):"));
        JScrollPane modScroll = new JScrollPane(modulesA);
        modScroll.setMaximumSize(new Dimension(500, 90));
        modScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(modScroll);
        p.add(Box.createVerticalStrut(14));

        JButton create = new JButton("Create Project with Skills & Modules");
        create.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        create.setBackground(new Color(40, 100, 200));
        create.setForeground(Color.WHITE);
        create.setOpaque(true);
        create.setContentAreaFilled(true);
        create.setBorderPainted(false);
        create.setFocusPainted(false);
        create.setFont(new Font("Segoe UI", Font.BOLD, 13));
        create.setAlignmentX(Component.LEFT_ALIGNMENT);
        create.addActionListener(e -> {
            String title = titleF.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a project title");
                return;
            }

            List<String> chosenSkills = new ArrayList<>();
            for (Map.Entry<String, JCheckBox> entry : skillBoxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    chosenSkills.add(entry.getKey());
                }
            }
            if (chosenSkills.isEmpty()) chosenSkills.add("Java");

            try {
                JsonObject body = new JsonObject();
                body.addProperty("facultyId", Session.getUserId());
                body.addProperty("title", title);
                body.addProperty("description", descA.getText());
                body.addProperty("teamSize", (Integer) sizeSpin.getValue());
                body.addProperty("status", "OPEN");

                JsonArray skArr = new JsonArray();
                for (String s : chosenSkills) skArr.add(s);
                body.add("requiredSkills", skArr);

                HttpResponse<String> resp = post(BASE + "/projects", gson.toJson(body));
                if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                    JsonObject created = gson.fromJson(resp.body(), JsonObject.class);
                    long pid = created.get("id").getAsLong();
                    int count = 0;
                    for (String line : modulesA.getText().split("\\n")) {
                        String mod = line.trim();
                        if (mod.isEmpty()) continue;
                        JsonObject t = new JsonObject();
                        t.addProperty("projectId", pid);
                        t.addProperty("title", mod);
                        t.addProperty("progressPercent", 0);
                        t.addProperty("status", "PENDING");
                        post(BASE + "/tasks", gson.toJson(t));
                        count++;
                    }
                    JOptionPane.showMessageDialog(this,
                            "Project created successfully!\nProject ID: " + pid +
                            "\nRequired Skills: " + String.join(", ", chosenSkills) +
                            "\nWork Modules: " + count);
                    titleF.setText("");
                    descA.setText("");
                    for (JCheckBox cb : skillBoxes.values()) cb.setSelected(false);
                    loadProjects();
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + resp.body());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        p.add(create);

        JScrollPane container = new JScrollPane(p);
        container.setOpaque(false);
        container.getViewport().setOpaque(false);
        container.setBorder(null);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(container, BorderLayout.CENTER);
        return wrapper;
    }

    // ==================== 3. RECOMMENDATIONS ====================
    private JPanel createRecoPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        JTextField projectIdF = new JTextField(8);
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Student ID", "Name", "Email", "Match Score", "Skills Matched"}, 0);
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Enter Project ID:"));
        top.add(projectIdF);
        JButton go = new JButton("Get Student Recommendations");
        go.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        top.add(go);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        go.addActionListener(e -> {
            model.setRowCount(0);
            String pid = projectIdF.getText().trim();
            if (pid.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a Project ID");
                return;
            }
            try {
                HttpResponse<String> resp = get(BASE + "/projects/" + pid + "/recommendations");
                JsonArray arr = gson.fromJson(resp.body(), JsonArray.class);
                if (arr == null || arr.size() == 0) {
                    JOptionPane.showMessageDialog(this, "No matching students found.");
                    return;
                }
                for (JsonElement el : arr) {
                    JsonObject o = el.getAsJsonObject();
                    model.addRow(new Object[]{
                            o.get("studentId").getAsLong(),
                            o.get("fullName").getAsString(),
                            o.get("email").getAsString(),
                            String.format("%.2f", o.get("score").getAsDouble()),
                            o.get("matchedSkills").getAsInt()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        return p;
    }

    // ==================== 4. SKILL GAP ANALYSIS ====================
    private JPanel createSkillGapPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        top.setOpaque(false);
        top.add(new JLabel("Project ID:"));
        gapProjectIdField = new JTextField(6);
        top.add(gapProjectIdField);

        top.add(new JLabel("Team ID (optional):"));
        gapTeamIdField = new JTextField(6);
        top.add(gapTeamIdField);

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

        gapOverviewLabel = new JLabel("Enter Project ID and click 'Run Skill Gap Analysis' to inspect team skill coverage.");
        gapOverviewLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gapOverviewLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 6, 6));
        center.add(gapOverviewLabel, BorderLayout.NORTH);

        gapTableModel = new DefaultTableModel(
                new String[]{"Required Skill", "Required Level", "Status", "Best Member / Student", "Member Level", "Score", "Gap Note"}, 0);
        JTable table = new JTable(gapTableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createTitledBorder("Recommendations to Close Skill Gaps"));
        gapRecommendationsArea = new JTextArea(4, 40);
        gapRecommendationsArea.setEditable(false);
        gapRecommendationsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bottom.add(new JScrollPane(gapRecommendationsArea), BorderLayout.CENTER);
        center.add(bottom, BorderLayout.SOUTH);

        p.add(center, BorderLayout.CENTER);

        analyzeBtn.addActionListener(e -> runSkillGapAnalysis());
        return p;
    }

    public void runSkillGapAnalysisFor(long projectId, Long teamId) {
        gapProjectIdField.setText(String.valueOf(projectId));
        if (teamId != null) gapTeamIdField.setText(String.valueOf(teamId));
        else gapTeamIdField.setText("");
        runSkillGapAnalysis();
    }

    private void runSkillGapAnalysis() {
        String pid = gapProjectIdField.getText().trim();
        if (pid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Project ID");
            return;
        }

        String tid = gapTeamIdField.getText().trim();
        String url = BASE + "/projects/" + pid + "/skill-gap";
        if (!tid.isEmpty()) {
            url += "?teamId=" + tid;
        }

        gapTableModel.setRowCount(0);
        gapRecommendationsArea.setText("");

        try {
            HttpResponse<String> resp = get(url);
            if (resp.statusCode() != 200) {
                JOptionPane.showMessageDialog(this, "Error " + resp.statusCode() + ": " + resp.body());
                return;
            }
            JsonObject obj = gson.fromJson(resp.body(), JsonObject.class);

            String title = obj.get("projectTitle").getAsString();
            String target = obj.get("targetName").getAsString();
            double cov = obj.get("coveragePercentage").getAsDouble();
            String gapStatus = obj.get("gapStatus").getAsString();
            int satisfied = obj.get("satisfiedCount").getAsInt();
            int deficient = obj.get("deficientCount").getAsInt();
            int missing = obj.get("missingCount").getAsInt();

            gapOverviewLabel.setText(String.format(
                    "<html><b>Project:</b> %s | <b>Target:</b> %s | <b>Coverage:</b> %.1f%% (%s) | "
                            + "<font color='green'>Satisfied: %d</font> | "
                            + "<font color='orange'>Deficient: %d</font> | "
                            + "<font color='red'>Missing: %d</font></html>",
                    title, target, cov, gapStatus, satisfied, deficient, missing));

            JsonArray skills = obj.getAsJsonArray("skills");
            if (skills != null) {
                for (JsonElement el : skills) {
                    JsonObject s = el.getAsJsonObject();
                    gapTableModel.addRow(new Object[]{
                            s.get("skillName").getAsString(),
                            s.get("requiredLevel").getAsString(),
                            s.get("status").getAsString(),
                            s.get("bestStudentName").getAsString(),
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

    // ==================== 5. TEAM APPROVALS ====================
    private JPanel createTeamApprovalsPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        teamsModel = new DefaultTableModel(
                new String[]{"Team ID", "Project ID", "Status", "Leader ID", "Members", "Note"}, 0);
        JTable table = new JTable(teamsModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        btns.setOpaque(false);
        JButton refresh = new JButton("Load Pending Teams");
        JButton checkGap = new JButton("Check Team Skill Gap");
        JButton approve = new JButton("Approve Selected");
        JButton reject = new JButton("Reject Selected");

        checkGap.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        checkGap.setBackground(new Color(230, 120, 20));
        checkGap.setForeground(Color.WHITE);
        checkGap.setOpaque(true);
        checkGap.setFont(new Font("Segoe UI", Font.BOLD, 13));

        approve.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        approve.setBackground(new Color(60, 160, 90));
        approve.setForeground(Color.WHITE);
        approve.setOpaque(true);
        approve.setFont(new Font("Segoe UI", Font.BOLD, 13));

        reject.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        reject.setBackground(new Color(200, 70, 70));
        reject.setForeground(Color.WHITE);
        reject.setOpaque(true);
        reject.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btns.add(refresh);
        btns.add(checkGap);
        btns.add(approve);
        btns.add(reject);
        p.add(btns, BorderLayout.SOUTH);

        refresh.addActionListener(e -> loadPendingTeams());
        checkGap.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a pending team from the table first");
                return;
            }
            long tid = (Long) teamsModel.getValueAt(row, 0);
            long pid = (Long) teamsModel.getValueAt(row, 1);
            runSkillGapAnalysisFor(pid, tid);
            JOptionPane.showMessageDialog(this, "Skill Gap Analysis loaded in the 'Skill Gap Analysis' tab for Team #" + tid);
        });
        approve.addActionListener(e -> decideTeam(table, "APPROVE"));
        reject.addActionListener(e -> decideTeam(table, "REJECT"));
        return p;
    }

    private void loadPendingTeams() {
        teamsModel.setRowCount(0);
        try {
            HttpResponse<String> resp = get(BASE + "/teams/pending");
            JsonArray arr = gson.fromJson(resp.body(), JsonArray.class);
            if (arr == null || arr.size() == 0) {
                JOptionPane.showMessageDialog(this, "No pending team requests.");
                return;
            }
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                StringBuilder names = new StringBuilder();
                if (o.has("members") && o.get("members").isJsonArray()) {
                    for (JsonElement m : o.getAsJsonArray("members")) {
                        if (names.length() > 0) names.append(", ");
                        names.append(m.getAsJsonObject().get("name").getAsString());
                    }
                }
                teamsModel.addRow(new Object[]{
                        o.get("id").getAsLong(),
                        o.get("projectId").getAsLong(),
                        o.get("status").getAsString(),
                        o.has("createdBy") ? o.get("createdBy").getAsLong() : "—",
                        names.toString(),
                        o.has("note") ? o.get("note").getAsString() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void decideTeam(JTable table, String action) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a team row first");
            return;
        }
        long teamId = (Long) teamsModel.getValueAt(row, 0);
        try {
            JsonObject body = new JsonObject();
            body.addProperty("action", action);
            HttpResponse<String> resp = post(BASE + "/teams/" + teamId + "/decision", gson.toJson(body));
            if (resp.statusCode() == 200) {
                JOptionPane.showMessageDialog(this, "Team " + action.toLowerCase() + "d successfully!");
                loadPendingTeams();
                loadProjects();
            } else {
                JOptionPane.showMessageDialog(this, "Failed: " + resp.body());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ==================== 6. PROJECT PROGRESS ====================
    private JPanel createProgressPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.setOpaque(false);
        top.add(new JLabel("Project ID:"));
        progressProjectIdField = new JTextField(6);
        top.add(progressProjectIdField);

        JButton load = new JButton("Load Progress");
        JButton addMod = new JButton("Add Module");
        JButton setPct = new JButton("Update Selected %");
        top.add(load);
        top.add(addMod);
        top.add(setPct);
        p.add(top, BorderLayout.NORTH);

        progressSummaryLabel = new JLabel("Enter Project ID → Load Progress Dashboard.");
        progressSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        progressSummaryLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        progressModel = new DefaultTableModel(new String[]{"Module", "Progress %", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(progressModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(progressSummaryLabel, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);

        load.addActionListener(e -> loadProgress());

        setPct.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= progressTaskIds.size()) {
                JOptionPane.showMessageDialog(this, "Select a module row first");
                return;
            }
            String input = JOptionPane.showInputDialog(this, "Enter progress % (0-100):", "50");
            if (input == null) return;
            try {
                int pct = Integer.parseInt(input.trim());
                long taskId = progressTaskIds.get(row);
                JsonObject body = new JsonObject();
                body.addProperty("progressPercent", pct);
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks/" + taskId + "/progress"))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                        .build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) loadProgress();
                else JOptionPane.showMessageDialog(this, "Failed: " + resp.body());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        addMod.addActionListener(e -> {
            String pid = progressProjectIdField.getText().trim();
            if (pid.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Type the Project ID first");
                return;
            }
            String name = JOptionPane.showInputDialog(this, "New module name:\n(e.g. Frontend, Backend, Database, Testing)");
            if (name == null || name.trim().isEmpty()) return;
            try {
                JsonObject t = new JsonObject();
                t.addProperty("projectId", Long.parseLong(pid));
                t.addProperty("title", name.trim());
                t.addProperty("progressPercent", 0);
                t.addProperty("status", "PENDING");
                HttpResponse<String> resp = post(BASE + "/tasks", gson.toJson(t));
                if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                    loadProgress();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed: " + resp.body());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        return p;
    }

    private void loadProgress() {
        progressModel.setRowCount(0);
        progressTaskIds.clear();
        String pid = progressProjectIdField.getText().trim();
        if (pid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Project ID");
            return;
        }
        try {
            HttpResponse<String> resp = get(BASE + "/tasks/project/" + pid + "/progress");
            if (resp.statusCode() != 200) {
                JOptionPane.showMessageDialog(this, "Error: " + resp.body());
                return;
            }
            JsonObject o = gson.fromJson(resp.body(), JsonObject.class);
            double pct = o.get("percentComplete").getAsDouble();
            long done = o.get("completed").getAsLong();
            long ip = o.get("inProgress").getAsLong();
            long pend = o.get("pending").getAsLong();
            int total = o.get("total").getAsInt();
            String assignedLabel = o.has("assignedTo") ? o.get("assignedTo").getAsString() : "Not assigned";

            progressSummaryLabel.setText(String.format(
                    "<html><b>Assigned to:</b> %s &nbsp;|&nbsp; <b>Overall Progress:</b> %.1f%% &nbsp;|&nbsp; "
                            + "<font color='green'>Completed: %d</font> &nbsp;|&nbsp; "
                            + "<font color='orange'>In Progress: %d</font> &nbsp;|&nbsp; "
                            + "<font color='gray'>Pending: %d</font> (Total: %d)</html>",
                    assignedLabel, pct, done, ip, pend, total));

            JsonArray tasks = o.getAsJsonArray("tasks");
            if (tasks != null) {
                for (JsonElement el : tasks) {
                    JsonObject t = el.getAsJsonObject();
                    progressTaskIds.add(Long.parseLong(t.get("id").getAsString()));
                    int pctMod = t.has("progressPercent") ? t.get("progressPercent").getAsInt() : 0;
                    progressModel.addRow(new Object[]{
                            t.get("task").getAsString(),
                            pctMod + "%",
                            t.get("status").getAsString()
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ==================== 7. TEAM PERFORMANCE ====================
    private JPanel createPerformancePanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        top.setOpaque(false);
        top.add(new JLabel("Project ID:"));
        perfProjectIdField = new JTextField(6);
        top.add(perfProjectIdField);

        JButton loadBtn = new JButton("Load Skill Progress & Performance");
        loadBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        loadBtn.setBackground(new Color(40, 100, 200));
        loadBtn.setForeground(Color.WHITE);
        loadBtn.setOpaque(true);
        loadBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        top.add(loadBtn);
        p.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);

        perfSummaryLabel = new JLabel("Enter Project ID to view Team Performance Index & Skill Progress across modules.");
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

        loadBtn.addActionListener(e -> loadPerformance());
        return p;
    }

    private void loadPerformance() {
        String pid = perfProjectIdField.getText().trim();
        if (pid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Project ID");
            return;
        }

        perfModulesModel.setRowCount(0);
        perfMembersModel.setRowCount(0);

        try {
            HttpResponse<String> resp = get(BASE + "/projects/" + pid + "/team-performance");
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
            int total = obj.get("totalTasks").getAsInt();
            long comp = obj.get("tasksCompleted").getAsLong();
            long ip = obj.get("tasksInProgress").getAsLong();
            long pend = obj.get("tasksPending").getAsLong();

            perfSummaryLabel.setText(String.format(
                    "<html><b>Project:</b> %s &nbsp;|&nbsp; <b>Leader:</b> %s &nbsp;|&nbsp; "
                            + "<b>Performance Index:</b> <font color='blue'>%.1f / 100 (%s)</font> &nbsp;|&nbsp; "
                            + "<b>Module Progress:</b> %.1f%% &nbsp;|&nbsp; <b>Skill Mastery Avg:</b> %.1f%%<br>"
                            + "Tasks Completed: %d &nbsp;|&nbsp; In Progress: %d &nbsp;|&nbsp; Pending: %d (Total: %d)</html>",
                    title, leader, perfScore, rating, taskAvg, skillAvg, comp, ip, pend, total));

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
    }

    // ==================== 8. POST COURSE QUESTIONS (NEW LECTURER FEATURE) ====================
    private JPanel createPostQuestionsPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // TOP: Post new question form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Post a New Question to a Specified Course / Skill"),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Course row
        JPanel courseRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        courseRow.setOpaque(false);
        courseRow.add(new JLabel("Select Course / Skill:"));
        questionCourseCombo = new JComboBox<>(new String[]{
                "Java", "Spring Boot", "MySQL", "React", "Python",
                "Machine Learning", "Git", "Project Management"
        });
        questionCourseCombo.setPreferredSize(new Dimension(180, 28));
        courseRow.add(questionCourseCombo);

        courseRow.add(new JLabel("OR Add New Course:"));
        customCourseField = new JTextField(12);
        courseRow.add(customCourseField);
        form.add(courseRow);

        // Question text
        JPanel qRow = new JPanel(new BorderLayout(6, 4));
        qRow.setOpaque(false);
        qRow.add(new JLabel("Question Description:"), BorderLayout.NORTH);
        questionTextArea = new JTextArea(2, 40);
        questionTextArea.setLineWrap(true);
        qRow.add(new JScrollPane(questionTextArea), BorderLayout.CENTER);
        form.add(qRow);
        form.add(Box.createVerticalStrut(6));

        // Options 2x2 grid
        JPanel optsGrid = new JPanel(new GridLayout(2, 2, 10, 6));
        optsGrid.setOpaque(false);

        optAField = new JTextField(20);
        optBField = new JTextField(20);
        optCField = new JTextField(20);
        optDField = new JTextField(20);

        optsGrid.add(labeledField("Option A:", optAField));
        optsGrid.add(labeledField("Option B:", optBField));
        optsGrid.add(labeledField("Option C:", optCField));
        optsGrid.add(labeledField("Option D:", optDField));
        form.add(optsGrid);
        form.add(Box.createVerticalStrut(8));

        // Correct Answer + Submit
        JPanel subRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        subRow.setOpaque(false);
        subRow.add(new JLabel("Correct Option:"));
        correctAnsCombo = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        correctAnsCombo.setPreferredSize(new Dimension(80, 28));
        subRow.add(correctAnsCombo);

        JButton postBtn = new JButton("Post Question to Course");
        postBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        postBtn.setBackground(new Color(60, 160, 90));
        postBtn.setForeground(Color.WHITE);
        postBtn.setOpaque(true);
        postBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        postBtn.addActionListener(e -> postNewQuestion());
        subRow.add(postBtn);
        form.add(subRow);

        p.add(form, BorderLayout.NORTH);

        // CENTER: Questions list with course filter
        JPanel listPanel = new JPanel(new BorderLayout(8, 8));
        listPanel.setOpaque(false);
        listPanel.setBorder(BorderFactory.createTitledBorder("Existing Course Questions (Including Pre-seeded & Lecturer Questions)"));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterBar.setOpaque(false);
        filterBar.add(new JLabel("Filter by Course:"));
        filterCourseCombo = new JComboBox<>(new String[]{"ALL"});
        filterCourseCombo.setPreferredSize(new Dimension(160, 28));
        filterCourseCombo.addActionListener(e -> {
            String selected = (String) filterCourseCombo.getSelectedItem();
            loadCourseQuestions(selected != null ? selected : "ALL");
        });
        filterBar.add(filterCourseCombo);

        JButton refreshBtn = new JButton("Refresh Questions");
        refreshBtn.addActionListener(e -> {
            loadCourses();
            loadCourseQuestions((String) filterCourseCombo.getSelectedItem());
        });
        filterBar.add(refreshBtn);
        listPanel.add(filterBar, BorderLayout.NORTH);

        questionsTableModel = new DefaultTableModel(
                new String[]{"ID", "Course", "Question", "Option A", "Option B", "Option C", "Option D", "Ans", "Author"}, 0);
        JTable qTable = new JTable(questionsTableModel);
        qTable.setRowHeight(28);
        qTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        qTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        listPanel.add(new JScrollPane(qTable), BorderLayout.CENTER);

        p.add(listPanel, BorderLayout.CENTER);

        return p;
    }

    private JPanel labeledField(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setOpaque(false);
        p.add(new JLabel(label), BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void loadCourses() {
        try {
            HttpResponse<String> resp = get(BASE + "/questions/courses");
            if (resp.statusCode() == 200) {
                JsonArray arr = gson.fromJson(resp.body(), JsonArray.class);
                if (arr != null) {
                    Set<String> set = new LinkedHashSet<>();
                    set.add("ALL");
                    for (JsonElement el : arr) set.add(el.getAsString());

                    filterCourseCombo.removeAllItems();
                    for (String c : set) filterCourseCombo.addItem(c);

                    // Also update questionCourseCombo
                    Set<String> postSet = new LinkedHashSet<>();
                    for (JsonElement el : arr) postSet.add(el.getAsString());
                    questionCourseCombo.removeAllItems();
                    for (String c : postSet) questionCourseCombo.addItem(c);
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadCourseQuestions(String courseFilter) {
        questionsTableModel.setRowCount(0);
        try {
            String url = (courseFilter == null || "ALL".equalsIgnoreCase(courseFilter))
                    ? BASE + "/questions"
                    : BASE + "/questions/course/" + courseFilter;

            HttpResponse<String> resp = get(url);
            if (resp.statusCode() == 200) {
                JsonArray arr = gson.fromJson(resp.body(), JsonArray.class);
                if (arr != null) {
                    for (JsonElement el : arr) {
                        JsonObject q = el.getAsJsonObject();
                        questionsTableModel.addRow(new Object[]{
                                q.get("id").getAsLong(),
                                q.get("course").getAsString(),
                                q.get("questionText").getAsString(),
                                q.get("optionA").getAsString(),
                                q.get("optionB").getAsString(),
                                q.get("optionC").getAsString(),
                                q.get("optionD").getAsString(),
                                q.get("correctAnswer").getAsString(),
                                q.has("createdByName") && !q.get("createdByName").isJsonNull()
                                        ? q.get("createdByName").getAsString() : "System"
                        });
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void postNewQuestion() {
        String course = customCourseField.getText().trim();
        if (course.isEmpty()) {
            course = (String) questionCourseCombo.getSelectedItem();
        }
        if (course == null || course.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select or type a Course name");
            return;
        }

        String qText = questionTextArea.getText().trim();
        String opA = optAField.getText().trim();
        String opB = optBField.getText().trim();
        String opC = optCField.getText().trim();
        String opD = optDField.getText().trim();
        String correct = (String) correctAnsCombo.getSelectedItem();

        if (qText.isEmpty() || opA.isEmpty() || opB.isEmpty() || opC.isEmpty() || opD.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in the question and all four options");
            return;
        }

        try {
            JsonObject body = new JsonObject();
            body.addProperty("course", course);
            body.addProperty("questionText", qText);
            body.addProperty("optionA", opA);
            body.addProperty("optionB", opB);
            body.addProperty("optionC", opC);
            body.addProperty("optionD", opD);
            body.addProperty("correctAnswer", correct);
            body.addProperty("createdByName", Session.getFullName() + " (Faculty)");

            HttpResponse<String> resp = post(BASE + "/questions", gson.toJson(body));
            if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                JOptionPane.showMessageDialog(this,
                        "Question posted successfully for course \"" + course + "\"!\nStudents can now answer it in the Skill Test.");
                questionTextArea.setText("");
                optAField.setText("");
                optBField.setText("");
                optCField.setText("");
                optDField.setText("");
                customCourseField.setText("");
                loadCourses();
                loadCourseQuestions("ALL");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to post question: " + resp.body());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ==================== 9. REPORTS ====================
    private JPanel createReportsPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        p.add(new JScrollPane(area), BorderLayout.CENTER);

        JButton gen = new JButton("Generate Comprehensive Faculty Report");
        gen.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        gen.setBackground(new Color(40, 100, 200));
        gen.setForeground(Color.WHITE);
        gen.setOpaque(true);
        gen.setFont(new Font("Segoe UI", Font.BOLD, 13));

        gen.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            sb.append("========================================================================\n");
            sb.append("                SKILLMATCH COMPREHENSIVE FACULTY REPORT\n");
            sb.append("                 One System · Two User Journeys Architecture\n");
            sb.append("========================================================================\n");
            sb.append("Generated for: ").append(Session.getFullName()).append("\n");
            sb.append("Timestamp:     ").append(new Date()).append("\n\n");

            try {
                HttpResponse<String> projResp = get(BASE + "/projects");
                JsonArray projects = gson.fromJson(projResp.body(), JsonArray.class);
                sb.append("TOTAL PROJECTS MANAGED: ").append(projects != null ? projects.size() : 0).append("\n");
                sb.append("------------------------------------------------------------------------\n");

                if (projects != null) {
                    for (JsonElement el : projects) {
                        JsonObject o = el.getAsJsonObject();
                        long pid = o.get("id").getAsLong();
                        sb.append("\n[Project #").append(pid).append("] ").append(o.get("title").getAsString()).append("\n");
                        sb.append("  Status:        ").append(o.get("status").getAsString()).append("\n");
                        sb.append("  Team Size:     ").append(o.get("teamSize").getAsInt()).append("\n");
                        sb.append("  Assigned To:   ").append(o.has("assignedTo") ? o.get("assignedTo").getAsString() : "Not assigned").append("\n");

                        if (o.has("requiredSkills") && o.get("requiredSkills").isJsonArray()) {
                            sb.append("  Req. Skills:   ");
                            JsonArray rsa = o.getAsJsonArray("requiredSkills");
                            for (int i = 0; i < rsa.size(); i++) {
                                if (i > 0) sb.append(", ");
                                sb.append(rsa.get(i).getAsString());
                            }
                            sb.append("\n");
                        }

                        try {
                            HttpResponse<String> pr = get(BASE + "/tasks/project/" + pid + "/progress");
                            JsonObject prog = gson.fromJson(pr.body(), JsonObject.class);
                            sb.append("  Module Progress: ").append(prog.get("percentComplete").getAsDouble()).append("%\n");
                        } catch (Exception ignored) {}

                        try {
                            HttpResponse<String> gapResp = get(BASE + "/projects/" + pid + "/skill-gap");
                            if (gapResp.statusCode() == 200) {
                                JsonObject go = gson.fromJson(gapResp.body(), JsonObject.class);
                                sb.append("  Skill Gap Status: ").append(go.get("gapStatus").getAsString())
                                        .append(" (Coverage: ").append(go.get("coveragePercentage").getAsDouble()).append("%)\n");
                            }
                        } catch (Exception ignored) {}
                    }
                }

                HttpResponse<String> qResp = get(BASE + "/questions");
                JsonArray questions = gson.fromJson(qResp.body(), JsonArray.class);
                sb.append("\n------------------------------------------------------------------------\n");
                sb.append("TOTAL COURSE QUESTIONS IN BANK: ").append(questions != null ? questions.size() : 0).append("\n");

                HttpResponse<String> teamResp = get(BASE + "/teams/pending");
                JsonArray pending = gson.fromJson(teamResp.body(), JsonArray.class);
                sb.append("PENDING TEAM APPROVAL REQUESTS: ").append(pending != null ? pending.size() : 0).append("\n");

                sb.append("\n========================================================================\n");
                sb.append("                         END OF REPORT\n");
                sb.append("========================================================================\n");
            } catch (Exception ex) {
                sb.append("Error generating report: ").append(ex.getMessage());
            }
            area.setText(sb.toString());
        });

        p.add(gen, BorderLayout.SOUTH);
        return p;
    }

    private HttpResponse<String> get(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String url, String json) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}
