USE skillmatch;

-- Demo users (password = "password123")
INSERT INTO users (email, password_hash, full_name, role) VALUES
('student1@college.edu', '$2a$10$demoHashStudent1', 'Aarav Sharma', 'STUDENT'),
('student2@college.edu', '$2a$10$demoHashStudent2', 'Priya Patel', 'STUDENT'),
('student3@college.edu', '$2a$10$demoHashStudent3', 'Rohan Gupta', 'STUDENT'),
('faculty1@college.edu', '$2a$10$demoHashFaculty1', 'Dr. Meera Nair', 'FACULTY'),
('faculty2@college.edu', '$2a$10$demoHashFaculty2', 'Prof. Vikram Singh', 'FACULTY');

INSERT INTO student_profiles (user_id, department, year_of_study, bio) VALUES
(1, 'Computer Science', 3, 'Passionate about backend systems and AI'),
(2, 'Information Technology', 2, 'UI/UX and frontend enthusiast'),
(3, 'Computer Science', 4, 'Full-stack developer and team lead experience');

INSERT INTO skills (name, category) VALUES
('Java', 'Programming'),
('Spring Boot', 'Backend'),
('MySQL', 'Database'),
('React', 'Frontend'),
('Python', 'Programming'),
('Machine Learning', 'AI'),
('Project Management', 'Soft Skill'),
('Git', 'Tools');

INSERT INTO student_skills (student_id, skill_id, level, test_score) VALUES
(1, 1, 'ADVANCED', 88.50),
(1, 2, 'INTERMEDIATE', 76.00),
(1, 3, 'ADVANCED', 91.00),
(2, 4, 'ADVANCED', 85.00),
(2, 1, 'INTERMEDIATE', 70.00),
(2, 8, 'ADVANCED', 92.00),
(3, 1, 'EXPERT', 95.00),
(3, 2, 'ADVANCED', 89.00),
(3, 5, 'INTERMEDIATE', 78.00),
(3, 7, 'ADVANCED', 84.00);

INSERT INTO skill_tests (skill_id, question, option_a, option_b, option_c, option_d, correct_option) VALUES
(1, 'Which keyword is used to inherit a class in Java?', 'implements', 'extends', 'inherits', 'super', 'B'),
(1, 'What is the default value of a boolean variable in Java?', 'true', 'false', 'null', '0', 'B'),
(2, 'Which annotation is used to mark a Spring Boot main class?', '@SpringBootApplication', '@EnableAutoConfiguration', '@Configuration', '@Component', 'A'),
(3, 'Which SQL clause is used to filter rows?', 'ORDER BY', 'GROUP BY', 'WHERE', 'HAVING', 'C');

INSERT INTO projects (faculty_id, title, description, team_size, status) VALUES
(4, 'Campus Event Management System', 'Build a full-stack system for managing college events with registration and analytics.', 4, 'OPEN'),
(4, 'AI-Based Attendance System', 'Prototype using face recognition and dashboard for faculty.', 3, 'OPEN'),
(5, 'Skill Portfolio Web App', 'Students can showcase projects and skills with verification.', 3, 'OPEN');

INSERT INTO project_required_skills (project_id, skill_id, min_level) VALUES
(1, 1, 'INTERMEDIATE'),
(1, 2, 'INTERMEDIATE'),
(1, 3, 'INTERMEDIATE'),
(1, 7, 'BEGINNER'),
(2, 5, 'INTERMEDIATE'),
(2, 6, 'BEGINNER'),
(2, 1, 'INTERMEDIATE'),
(3, 1, 'ADVANCED'),
(3, 4, 'INTERMEDIATE'),
(3, 8, 'INTERMEDIATE');
