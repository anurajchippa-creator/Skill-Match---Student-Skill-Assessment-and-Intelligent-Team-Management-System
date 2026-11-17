package com.skillmatch.controller;

import com.skillmatch.dto.ProjectDTO;
import com.skillmatch.model.*;
import com.skillmatch.repository.*;
import com.skillmatch.service.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final MatchingService matchingService;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final ProjectRequiredSkillRepository projectRequiredSkillRepository;
    private final SkillRepository skillRepository;

    public ProjectController(ProjectRepository projectRepository,
                             MatchingService matchingService,
                             TeamRepository teamRepository,
                             TeamMemberRepository teamMemberRepository,
                             UserRepository userRepository,
                             ProjectRequiredSkillRepository projectRequiredSkillRepository,
                             SkillRepository skillRepository) {
        this.projectRepository = projectRepository;
        this.matchingService = matchingService;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.projectRequiredSkillRepository = projectRequiredSkillRepository;
        this.skillRepository = skillRepository;
    }

    @GetMapping
    public List<ProjectDTO> getAll() {
        return projectRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getById(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(p -> ResponseEntity.ok(toDto(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ProjectDTO create(@RequestBody Map<String, Object> body) {
        Project project = new Project();
        project.setFacultyId(Long.valueOf(body.get("facultyId").toString()));
        project.setTitle(body.get("title").toString());
        project.setDescription(body.get("description") != null ? body.get("description").toString() : "");
        project.setTeamSize(body.get("teamSize") != null ? Integer.valueOf(body.get("teamSize").toString()) : 3);
        project.setStatus(Project.Status.OPEN);
        project.setAssignedTeamId(null);

        Project saved = projectRepository.save(project);

        // Check if requiredSkills were passed
        if (body.containsKey("requiredSkills")) {
            Object reqObj = body.get("requiredSkills");
            if (reqObj instanceof List<?> list) {
                for (Object item : list) {
                    if (item == null) continue;
                    String skillName = item.toString().trim();
                    if (skillName.isEmpty()) continue;
                    saveRequiredSkill(saved, skillName, ProjectRequiredSkill.SkillLevel.INTERMEDIATE);
                }
            }
        }

        return toDto(saved);
    }

    @GetMapping("/{id}/required-skills")
    public List<Map<String, Object>> getRequiredSkills(@PathVariable Long id) {
        return projectRequiredSkillRepository.findByProject_Id(id).stream()
                .filter(rs -> rs.getSkill() != null)
                .map(rs -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", rs.getId());
                    m.put("skillName", rs.getSkill().getName());
                    m.put("category", rs.getSkill().getCategory());
                    m.put("minLevel", rs.getMinLevel() != null ? rs.getMinLevel().name() : "INTERMEDIATE");
                    return m;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/required-skills")
    public ResponseEntity<?> addRequiredSkill(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<Project> pOpt = projectRepository.findById(id);
        if (pOpt.isEmpty()) return ResponseEntity.notFound().build();

        String skillName = body.get("skillName");
        if (skillName == null || skillName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "skillName is required"));
        }

        String levelStr = body.getOrDefault("minLevel", "INTERMEDIATE");
        ProjectRequiredSkill.SkillLevel minLevel;
        try {
            minLevel = ProjectRequiredSkill.SkillLevel.valueOf(levelStr.toUpperCase());
        } catch (Exception e) {
            minLevel = ProjectRequiredSkill.SkillLevel.INTERMEDIATE;
        }

        ProjectRequiredSkill saved = saveRequiredSkill(pOpt.get(), skillName.trim(), minLevel);
        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "skillName", saved.getSkill().getName(),
                "minLevel", saved.getMinLevel().name()
        ));
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<?> recommendations(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(matchingService.recommendStudents(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/skill-gap")
    public ResponseEntity<?> getSkillGap(@PathVariable Long id,
                                         @RequestParam(required = false) Long teamId,
                                         @RequestParam(required = false) Long studentId) {
        try {
            return ResponseEntity.ok(matchingService.calculateSkillGap(id, teamId, studentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/team-performance")
    public ResponseEntity<?> getTeamPerformance(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(matchingService.calculateTeamPerformance(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/student/{studentId}/matches")
    public ResponseEntity<?> matchForStudent(@PathVariable Long studentId) {
        try {
            return ResponseEntity.ok(matchingService.matchProjectsForStudent(studentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/faculty/{facultyId}")
    public List<ProjectDTO> byFaculty(@PathVariable Long facultyId) {
        return projectRepository.findByFacultyId(facultyId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ProjectRequiredSkill saveRequiredSkill(Project project, String skillName, ProjectRequiredSkill.SkillLevel minLevel) {
        Skill skill = skillRepository.findByName(skillName)
                .orElseGet(() -> skillRepository.save(Skill.builder()
                        .name(skillName)
                        .category("Technical")
                        .build()));

        // check if already assigned to project
        List<ProjectRequiredSkill> existing = projectRequiredSkillRepository.findByProject_Id(project.getId());
        for (ProjectRequiredSkill prs : existing) {
            if (prs.getSkill() != null && prs.getSkill().getId().equals(skill.getId())) {
                prs.setMinLevel(minLevel);
                return projectRequiredSkillRepository.save(prs);
            }
        }

        ProjectRequiredSkill req = ProjectRequiredSkill.builder()
                .project(project)
                .skill(skill)
                .minLevel(minLevel)
                .build();
        return projectRequiredSkillRepository.save(req);
    }

    private ProjectDTO toDto(Project p) {
        String label = "Not assigned";
        if (p.getAssignedTeamId() != null) {
            label = buildAssignedLabel(p.getAssignedTeamId());
        }
        List<String> reqSkills = Collections.emptyList();
        try {
            List<ProjectRequiredSkill> list = projectRequiredSkillRepository.findByProject_Id(p.getId());
            if (list != null) {
                reqSkills = list.stream()
                        .filter(rs -> rs.getSkill() != null)
                        .map(rs -> rs.getSkill().getName())
                        .collect(Collectors.toList());
            }
        } catch (Exception ignored) {}

        return ProjectDTO.from(p, label, reqSkills);
    }

    private String buildAssignedLabel(Long teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) return "Team #" + teamId;
        Team team = teamOpt.get();
        String leaderName = "Unknown";
        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
        for (TeamMember m : members) {
            if ("LEADER".equalsIgnoreCase(m.getRoleInTeam())) {
                leaderName = userRepository.findById(m.getStudentId())
                        .map(User::getFullName).orElse("Unknown");
                break;
            }
        }
        if ("Unknown".equals(leaderName) && team.getCreatedBy() != null) {
            leaderName = userRepository.findById(team.getCreatedBy())
                    .map(User::getFullName).orElse("Unknown");
        }
        return "Team #" + teamId + " (Leader: " + leaderName + ")";
    }
}
