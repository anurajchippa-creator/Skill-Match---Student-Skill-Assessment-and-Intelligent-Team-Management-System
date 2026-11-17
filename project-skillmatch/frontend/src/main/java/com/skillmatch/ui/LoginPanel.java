package com.skillmatch.ui;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class LoginPanel extends JPanel {

    private final MainFrame mainFrame;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel formsPanel = new JPanel(cardLayout);

    // Login Fields
    private final JTextField loginEmailField = new JTextField(20);
    private final JPasswordField loginPasswordField = new JPasswordField(20);

    // Register Fields
    private final JTextField regNameField = new JTextField(20);
    private final JTextField regEmailField = new JTextField(20);
    private final JPasswordField regPasswordField = new JPasswordField(20);
    private final JComboBox<String> regRoleCombo = new JComboBox<>(new String[]{"STUDENT", "FACULTY"});

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private static final String BASE_URL = "http://localhost:8080/api";

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 250));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 235), 1),
                BorderFactory.createEmptyBorder(25, 35, 25, 35)
        ));
        card.setPreferredSize(new Dimension(460, 520));

        JLabel title = new JLabel("SkillMatch");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(new Color(30, 60, 120));

        JLabel subtitle = new JLabel("One System · Two User Journeys");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(Color.GRAY);

        // Sub-panels for Login and Register
        formsPanel.setOpaque(false);
        formsPanel.add(createLoginForm(), "LOGIN_FORM");
        formsPanel.add(createRegisterForm(), "REGISTER_FORM");

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(16));
        card.add(formsPanel);

        add(card);
    }

    private JPanel createLoginForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel h = new JLabel("Login to Your Account");
        h.setFont(new Font("Segoe UI", Font.BOLD, 15));
        h.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(14));

        loginEmailField.setText("student1@college.edu");
        loginPasswordField.setText("Password123");

        p.add(labeled("Email Address or Roll Number:", loginEmailField));
        p.add(Box.createVerticalStrut(10));
        p.add(labeled("Password (Default: Password123):", loginPasswordField));
        p.add(Box.createVerticalStrut(16));

        JButton loginBtn = createButton("Login", new Color(40, 100, 200));
        loginBtn.addActionListener(e -> doLogin());
        p.add(loginBtn);
        p.add(Box.createVerticalStrut(10));

        JButton toRegisterBtn = createButton("Register New Account (Any Email)", new Color(60, 160, 90));
        toRegisterBtn.addActionListener(e -> cardLayout.show(formsPanel, "REGISTER_FORM"));
        p.add(toRegisterBtn);

        p.add(Box.createVerticalStrut(14));

        // Quick demo buttons
        JPanel demoBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        demoBar.setOpaque(false);
        JButton demoStudent = new JButton("Demo Student");
        demoStudent.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        demoStudent.addActionListener(e -> {
            loginEmailField.setText("student1@college.edu");
            loginPasswordField.setText("Password123");
        });

        JButton demoFaculty = new JButton("Demo Faculty");
        demoFaculty.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        demoFaculty.addActionListener(e -> {
            loginEmailField.setText("faculty1@college.edu");
            loginPasswordField.setText("Password123");
        });

        demoBar.add(demoStudent);
        demoBar.add(demoFaculty);
        p.add(demoBar);

        return p;
    }

    private JPanel createRegisterForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel h = new JLabel("Create Account · Register Any Email");
        h.setFont(new Font("Segoe UI", Font.BOLD, 15));
        h.setAlignmentX(Component.CENTER_ALIGNMENT);
        h.setForeground(new Color(60, 160, 90));
        p.add(h);
        p.add(Box.createVerticalStrut(12));

        regNameField.setText("");
        regEmailField.setText("");
        regPasswordField.setText("Password123"); // keep same convenient default

        p.add(labeled("Full Name:", regNameField));
        p.add(Box.createVerticalStrut(8));
        p.add(labeled("Email (Use ANY mail you wish, e.g. gmail, yahoo):", regEmailField));
        p.add(Box.createVerticalStrut(8));
        p.add(labeled("Password:", regPasswordField));
        p.add(Box.createVerticalStrut(8));
        p.add(labeled("Role:", regRoleCombo));
        p.add(Box.createVerticalStrut(14));

        JButton registerBtn = createButton("Register Now", new Color(60, 160, 90));
        registerBtn.addActionListener(e -> doRegister());
        p.add(registerBtn);
        p.add(Box.createVerticalStrut(8));

        JButton backBtn = createButton("Back to Login", new Color(100, 115, 130));
        backBtn.addActionListener(e -> cardLayout.show(formsPanel, "LOGIN_FORM"));
        p.add(backBtn);

        return p;
    }

    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setMaximumSize(new Dimension(340, 36));
        btn.setPreferredSize(new Dimension(340, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void doLogin() {
        String email = loginEmailField.getText().trim();
        String password = new String(loginPasswordField.getPassword());
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email and password");
            return;
        }
        try {
            String json = gson.toJson(java.util.Map.of("email", email, "password", password));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                JsonObject obj = gson.fromJson(resp.body(), JsonObject.class);
                String role = obj.get("role").getAsString();
                Session.setUser(obj.get("id").getAsLong(),
                        obj.get("fullName").getAsString(),
                        role);
                if ("STUDENT".equals(role)) {
                    mainFrame.showScreen("STUDENT");
                } else {
                    mainFrame.showScreen("FACULTY");
                }
            } else {
                String msg = "Invalid credentials. Please check your email and password.";
                try {
                    JsonObject err = gson.fromJson(resp.body(), JsonObject.class);
                    if (err.has("error")) msg = err.get("error").getAsString();
                } catch (Exception ignored) {}
                JOptionPane.showMessageDialog(this, msg);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot connect to backend server:\n" + ex.getMessage());
        }
    }

    private void doRegister() {
        String fullName = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        String role = (String) regRoleCombo.getSelectedItem();

        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your full name");
            return;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter any email of your choice");
            return;
        }
        if (password.isEmpty()) {
            password = "Password123";
        }

        try {
            String json = gson.toJson(java.util.Map.of(
                    "email", email,
                    "password", password,
                    "fullName", fullName,
                    "role", role
            ));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JOptionPane.showMessageDialog(this,
                        "Registration successful!\n\nEmail: " + email + "\nRole: " + role +
                        "\n\nYou can now log in immediately.");
                loginEmailField.setText(email);
                loginPasswordField.setText(password);
                cardLayout.show(formsPanel, "LOGIN_FORM");
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed: " + resp.body());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to server: " + ex.getMessage());
        }
    }
}
