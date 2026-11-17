package com.skillmatch.controller;

import com.skillmatch.model.*;
import com.skillmatch.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentSkillRepository studentSkillRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;

    public StudentController(StudentSkillRepository studentSkillRepository,
                             SkillRepository skillRepository,
                             UserRepository userRepository,
                             TeamMemberRepository teamMemberRepository,
                             TeamRepository teamRepository,
                             ProjectRepository projectRepository) {
        this.studentSkillRepository = studentSkillRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.teamRepository = teamRepository;
        this.projectRepository = projectRepository;
    }

    @GetMapping("/{studentId}/profile")
    public ResponseEntity<?> getProfile(@PathVariable Long studentId) {
        Optional<User> uOpt = userRepository.findById(studentId);
        if (uOpt.isEmpty()) return ResponseEntity.notFound().build();
        User u = uOpt.get();

        List<StudentSkill> skills = studentSkillRepository.findByStudentId(studentId);
        int totalSkills = skills.size();
        long verifiedSkills = skills.stream()
                .filter(s -> s.getTestScore() != null && s.getTestScore().doubleValue() > 0)
                .count();

        double avgScore = skills.stream()
                .map(StudentSkill::getTestScore)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(0.0);

        // Check if student belongs to an approved team/project
        String assignedProjectTitle = "Not Assigned";
        String teamRole = "None";
        Long assignedProjectId = null;
        Long teamId = null;

        List<TeamMember> memberships = teamMemberRepository.findByStudentId(studentId);
        for (TeamMember tm : memberships) {
            Optional<Team> tOpt = teamRepository.findById(tm.getTeamId());
            if (tOpt.isPresent() && (tOpt.get().getStatus() == Team.Status.APPROVED || tOpt.get().getStatus() == Team.Status.ACTIVE)) {
                teamId = tOpt.get().getId();
                teamRole = tm.getRoleInTeam();
                assignedProjectId = tOpt.get().getProjectId();
                Optional<Project> pOpt = projectRepository.findById(assignedProjectId);
                if (pOpt.isPresent()) {
                    assignedProjectTitle = pOpt.get().getTitle();
                }
                break;
            }
        }

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", u.getId());
        profile.put("fullName", u.getFullName());
        profile.put("email", u.getEmail());
        profile.put("role", u.getRole().name());
        profile.put("rollNumber", u.getRollNumber() != null ? u.getRollNumber() : "—");
        profile.put("totalSkills", totalSkills);
        profile.put("verifiedSkills", verifiedSkills);
        profile.put("averageTestScore", Math.round(avgScore * 10.0) / 10.0);
        profile.put("teamId", teamId != null ? teamId : "—");
        profile.put("teamRole", teamRole);
        profile.put("assignedProjectId", assignedProjectId != null ? assignedProjectId : "—");
        profile.put("assignedProjectTitle", assignedProjectTitle);
        profile.put("skills", skills);

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{studentId}/skills")
    public List<StudentSkill> getSkills(@PathVariable Long studentId) {
        return studentSkillRepository.findByStudentId(studentId);
    }

    @PostMapping("/{studentId}/skills")
    public StudentSkill addOrUpdateSkill(@PathVariable Long studentId,
                                         @RequestBody StudentSkill incoming) {
        // Resolve skill by name (create if missing)
        Skill skill = incoming.getSkill();
        if (skill != null && skill.getName() != null) {
            Optional<Skill> existing = skillRepository.findByName(skill.getName());
            if (existing.isPresent()) {
                skill = existing.get();
            } else {
                skill = skillRepository.save(Skill.builder()
                        .name(skill.getName())
                        .category(skill.getCategory() != null ? skill.getCategory() : "General")
                        .build());
            }
        }

        // Check if student already has this skill → update score/level
        List<StudentSkill> existingSkills = studentSkillRepository.findByStudentId(studentId);
        for (StudentSkill ss : existingSkills) {
            if (ss.getSkill() != null && skill != null
                    && ss.getSkill().getId().equals(skill.getId())) {
                if (incoming.getLevel() != null) ss.setLevel(incoming.getLevel());
                if (incoming.getTestScore() != null) ss.setTestScore(incoming.getTestScore());
                return studentSkillRepository.save(ss);
            }
        }

        // New skill for this student
        StudentSkill newSs = StudentSkill.builder()
                .studentId(studentId)
                .skill(skill)
                .level(incoming.getLevel() != null ? incoming.getLevel() : StudentSkill.SkillLevel.BEGINNER)
                .testScore(incoming.getTestScore() != null ? incoming.getTestScore() : BigDecimal.ZERO)
                .build();
        return studentSkillRepository.save(newSs);
    }
}
