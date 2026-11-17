package com.skillmatch.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Main entry point for the SkillMatch Java Swing desktop client.
 * One System · Two User Journeys
 */
public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    public MainFrame() {
        setTitle("SkillMatch – One System · Two User Journeys");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 780);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Screens
        mainPanel.add(new LoginPanel(this), "LOGIN");
        mainPanel.add(new StudentDashboard(this), "STUDENT");
        mainPanel.add(new FacultyDashboard(this), "FACULTY");

        add(mainPanel);
        showScreen("LOGIN");
    }

    public void showScreen(String name) {
        cardLayout.show(mainPanel, name);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Make tabbed panes noticeably larger, spacious, and modern across the entire app
        UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("TabbedPane.tabInsets", new Insets(10, 20, 10, 20));
        UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(2, 4, 2, 4));
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(10, 10, 10, 10));

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
