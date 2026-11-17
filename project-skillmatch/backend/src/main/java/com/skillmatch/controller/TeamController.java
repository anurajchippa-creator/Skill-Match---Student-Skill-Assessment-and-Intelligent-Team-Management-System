package com.skillmatch.controller;

import com.skillmatch.model.Project;
import com.skillmatch.model.Team;
import com.skillmatch.model.TeamMember;
import com.skillmatch.model.User;
import com.skillmatch.repository.ProjectRepository;
import com.skillmatch.repository.TeamMemberRepository;
import com.skillmatch.repository.TeamRepository;
import com.skillmatch.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public TeamController(TeamRepository teamRepository,
                          TeamMemberRepository teamMemberRepository,
                          UserRepository userRepository,
                          ProjectRepository projectRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    @PostMapping
    public ResponseEntity<?> proposeTeam(@RequestBody Map<String, Object> body) {
        Long projectId = Long.valueOf(body.get("projectId").toString());
        Long createdBy = Long.valueOf(body.get("createdBy").toString());
        String note = body.get("note") != null ? body.get("note").toString() : "";
        @SuppressWarnings("unchecked")
        List<Object> memberIds = body.get("memberIds") != null
                ? (List<Object>) body.get("memberIds") : List.of(createdBy);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Case 2 policy: one approved team per project
        if (project.getAssignedTeamId() != null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "This project is already assigned to Team #" + project.getAssignedTeamId()
                            + ". Another team cannot take the same project. Choose a different project or ask faculty to reopen it."
            ));
        }
        // Also block if any APPROVED/ACTIVE team already exists for this project
        List<Team> existing = teamRepository.findByProjectId(projectId);
        boolean taken = existing.stream().anyMatch(t ->
                t.getStatus() == Team.Status.APPROVED || t.getStatus() == Team.Status.ACTIVE);
        if (taken) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "A team is already approved for this project. Project is locked for new proposals."
            ));
        }

        Team team = Team.builder()
                .projectId(projectId)
                .createdBy(createdBy)
                .note(note)
                .status(Team.Status.PROPOSED)
                .build();
        team = teamRepository.save(team);

        Set<Long> unique = new LinkedHashSet<>();
        unique.add(createdBy);
        for (Object mid : memberIds) {
            unique.add(Long.valueOf(mid.toString()));
        }
        for (Long sid : unique) {
            teamMemberRepository.save(TeamMember.builder()
                    .teamId(team.getId())
                    .studentId(sid)
                    .roleInTeam(sid.equals(createdBy) ? "LEADER" : "MEMBER")
                    .build());
        }
        return ResponseEntity.ok(Map.of(
                "id", team.getId(),
                "status", team.getStatus().name(),
                "message", "Team proposed. Waiting for faculty approval."
        ));
    }

    @GetMapping
    public List<Map<String, Object>> listAll() {
        return teamRepository.findAll().stream().map(this::toMap).collect(Collectors.toList());
    }

    @GetMapping("/project/{projectId}")
    public List<Map<String, Object>> byProject(@PathVariable Long projectId) {
        return teamRepository.findByProjectId(projectId).stream()
                .map(this::toMap).collect(Collectors.toList());
    }

    @GetMapping("/pending")
    public List<Map<String, Object>> pending() {
        return teamRepository.findByStatus(Team.Status.PROPOSED).stream()
                .map(this::toMap).collect(Collectors.toList());
    }

    @PostMapping("/{teamId}/decision")
    public ResponseEntity<?> decision(@PathVariable Long teamId, @RequestBody Map<String, String> body) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        Project project = projectRepository.findById(team.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        String action = body.getOrDefault("action", "").toUpperCase();
        if ("APPROVE".equals(action)) {
            if (project.getAssignedTeamId() != null
                    && !project.getAssignedTeamId().equals(teamId)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Project already assigned to Team #" + project.getAssignedTeamId()
                ));
            }
            team.setStatus(Team.Status.APPROVED);
            teamRepository.save(team);
            // Assign project to this team and mark in progress
            project.setAssignedTeamId(team.getId());
            project.setStatus(Project.Status.IN_PROGRESS);
            projectRepository.save(project);
            // Auto-reject other pending proposals for same project
            for (Team other : teamRepository.findByProjectId(project.getId())) {
                if (!other.getId().equals(teamId) && other.getStatus() == Team.Status.PROPOSED) {
                    other.setStatus(Team.Status.REJECTED);
                    teamRepository.save(other);
                }
            }
        } else if ("REJECT".equals(action)) {
            team.setStatus(Team.Status.REJECTED);
            teamRepository.save(team);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "action must be APPROVE or REJECT"));
        }
        Map<String, Object> result = toMap(team);
        result.put("projectAssignedTo", project.getAssignedTeamId() != null
                ? "Team #" + project.getAssignedTeamId() : "Not assigned");
        result.put("projectStatus", project.getStatus().name());
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> toMap(Team t) {
        List<TeamMember> members = teamMemberRepository.findByTeamId(t.getId());
        List<Map<String, Object>> memberInfo = new ArrayList<>();
        for (TeamMember m : members) {
            Optional<User> u = userRepository.findById(m.getStudentId());
            memberInfo.add(Map.of(
                    "studentId", m.getStudentId(),
                    "role", m.getRoleInTeam(),
                    "name", u.map(User::getFullName).orElse("Unknown")
            ));
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", t.getId());
        map.put("projectId", t.getProjectId());
        map.put("status", t.getStatus().name());
        map.put("createdBy", t.getCreatedBy());
        map.put("note", t.getNote() != null ? t.getNote() : "");
        map.put("members", memberInfo);
        map.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : "");
        return map;
    }
}
