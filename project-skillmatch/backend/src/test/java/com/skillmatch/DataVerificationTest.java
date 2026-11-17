package com.skillmatch;

import com.skillmatch.config.DataLoader;
import com.skillmatch.model.User;
import com.skillmatch.repository.UserRepository;
import com.skillmatch.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DataVerificationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Test
    public void testStudentAndFacultyData() {
        // 1. Verify 64 AIML-D students exist
        assertEquals(64, DataLoader.STUDENTS_AIML_D.length, "Must have exactly 64 students in attendance list");

        for (String[] record : DataLoader.STUDENTS_AIML_D) {
            String rollNo = record[0];
            String name = record[1];

            Optional<User> userOpt = userRepository.findByRollNumberIgnoreCase(rollNo);
            assertTrue(userOpt.isPresent(), "Student with roll " + rollNo + " must exist");

            User u = userOpt.get();
            assertEquals(name, u.getFullName(), "Full name must match attendance sheet");
            assertEquals(User.Role.STUDENT, u.getRole(), "Role must be STUDENT");

            // Verify login by roll number
            Optional<User> loggedInByRoll = authService.login(rollNo, "Password123");
            assertTrue(loggedInByRoll.isPresent(), "Must be able to login with roll number " + rollNo);

            // Verify login by email
            Optional<User> loggedInByEmail = authService.login(u.getEmail(), "Password123");
            assertTrue(loggedInByEmail.isPresent(), "Must be able to login with email " + u.getEmail());
        }

        // 2. Verify old sample students are removed
        for (User u : userRepository.findAll()) {
            String nameLower = u.getFullName() != null ? u.getFullName().toLowerCase() : "";
            assertNotEquals("aarav sharma", nameLower, "Aarav Sharma must not exist");
            assertNotEquals("priya patel", nameLower, "Priya Patel must not exist");
            assertNotEquals("rohan gupta", nameLower, "Rohan Gupta must not exist");
            assertNotEquals("dr. meera nair", nameLower, "Dr. Meera Nair must not exist");
            assertNotEquals("prof. vikram singh", nameLower, "Prof. Vikram Singh must not exist");
        }

        // 3. Verify demo profiles still work
        Optional<User> demoStudent = authService.login("student1@college.edu", "Password123");
        assertTrue(demoStudent.isPresent(), "Demo student account must work");
        assertEquals("Demo Student", demoStudent.get().getFullName());

        Optional<User> demoFaculty = authService.login("faculty1@college.edu", "Password123");
        assertTrue(demoFaculty.isPresent(), "Demo faculty account must work");
        assertEquals("Demo Faculty", demoFaculty.get().getFullName());

        System.out.println(">>> ALL 64 STUDENTS AND AUTH VERIFICATIONS PASSED SUCCESSFULLY!");
    }
}

