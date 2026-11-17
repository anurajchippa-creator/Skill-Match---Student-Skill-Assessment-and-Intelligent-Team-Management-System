package com.skillmatch.controller;

import com.skillmatch.model.Question;
import com.skillmatch.model.Skill;
import com.skillmatch.repository.QuestionRepository;
import com.skillmatch.repository.SkillRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionController {

    private final QuestionRepository questionRepository;
    private final SkillRepository skillRepository;

    public QuestionController(QuestionRepository questionRepository,
                              SkillRepository skillRepository) {
        this.questionRepository = questionRepository;
        this.skillRepository = skillRepository;
    }

    @GetMapping
    public List<Question> getAll() {
        return questionRepository.findAll();
    }

    @GetMapping("/courses")
    public List<String> getCourses() {
        List<String> list = questionRepository.findDistinctCourses();
        if (list == null || list.isEmpty()) {
            return List.of("Java", "Spring Boot", "MySQL", "React", "Python", "Git", "Project Management", "Machine Learning");
        }
        return list;
    }

    @GetMapping("/course/{course}")
    public List<Question> getByCourse(@PathVariable String course) {
        return questionRepository.findByCourseIgnoreCase(course);
    }

    @PostMapping
    public ResponseEntity<?> createQuestion(@RequestBody Map<String, String> body) {
        String course = body.get("course");
        String questionText = body.get("questionText");
        String optionA = body.get("optionA");
        String optionB = body.get("optionB");
        String optionC = body.get("optionC");
        String optionD = body.get("optionD");
        String correctAnswer = body.get("correctAnswer");
        String createdByName = body.getOrDefault("createdByName", "Faculty");

        if (course == null || course.trim().isEmpty() ||
            questionText == null || questionText.trim().isEmpty() ||
            optionA == null || optionA.trim().isEmpty() ||
            optionB == null || optionB.trim().isEmpty() ||
            optionC == null || optionC.trim().isEmpty() ||
            optionD == null || optionD.trim().isEmpty() ||
            correctAnswer == null || correctAnswer.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
        }

        String cleanCourse = course.trim();
        String cleanAnswer = correctAnswer.trim().toUpperCase();
        if (!List.of("A", "B", "C", "D").contains(cleanAnswer)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Correct answer must be A, B, C, or D"));
        }

        // Also ensure Skill exists in skillRepository so students can add this skill
        if (skillRepository.findByName(cleanCourse).isEmpty()) {
            skillRepository.save(Skill.builder().name(cleanCourse).category("Technical").build());
        }

        Question q = Question.builder()
                .course(cleanCourse)
                .questionText(questionText.trim())
                .optionA(optionA.trim())
                .optionB(optionB.trim())
                .optionC(optionC.trim())
                .optionD(optionD.trim())
                .correctAnswer(cleanAnswer)
                .createdByName(createdByName)
                .build();

        Question saved = questionRepository.save(q);
        return ResponseEntity.ok(saved);
    }
}

