package com.skillmatch.controller;

import com.skillmatch.model.Project;
import com.skillmatch.model.Task;
import com.skillmatch.model.Team;
import com.skillmatch.model.TeamMember;
import com.skillmatch.repository.ProjectRepository;
import com.skillmatch.repository.TaskRepository;
import com.skillmatch.repository.TeamMemberRepository;
import com.skillmatch.repository.TeamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    public TaskController(TaskRepository taskRepository,
                          ProjectRepository projectRepository,
                          TeamRepository teamRepository,
                          TeamMemberRepository teamMemberRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @GetMapping("/project/{projectId}")
    public List<Task> byProject(@PathVariable Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    @PostMapping
    public Task create(@RequestBody Task task) {
        task.setId(null);
        if (task.getProgressPercent() == null) task.setProgressPercent(0);
        task.syncStatusFromPercent();
        return taskRepository.save(task);
    }

    /** Faculty or team leader updates status (legacy) */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        try {
            Task.Status st = Task.Status.valueOf(body.get("status").toUpperCase());
            task.setStatus(st);
            if (st == Task.Status.COMPLETED) task.setProgressPercent(100);
            else if (st == Task.Status.PENDING) task.setProgressPercent(0);
            else if (task.getProgressPercent() == null || task.getProgressPercent() == 0)
                task.setProgressPercent(50);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
        }
        return ResponseEntity.ok(taskRepository.save(task));
    }

    /**
     * Update module progress percent (0-100).
     * Allowed: faculty, or LEADER of the team assigned to this project.
     */
    @PutMapping("/{id}/progress")
    public ResponseEntity<?> updateProgress(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module/task not found"));

        int percent;
        try {
            percent = Integer.parseInt(body.get("progressPercent").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "progressPercent must be 0-100"));
        }
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;

        // Optional auth: studentId of caller
        if (body.containsKey("studentId")) {
            Long studentId = Long.valueOf(body.get("studentId").toString());
            if (!canUpdate(task.getProjectId(), studentId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "error", "Only the team leader of the assigned team (or faculty) can update progress."
                ));
            }
        }

        task.setProgressPercent(percent);
        task.syncStatusFromPercent();
        return ResponseEntity.ok(taskRepository.save(task));
    }

    private boolean canUpdate(Long projectId, Long studentId) {
        Optional<Project> p = projectRepository.findById(projectId);
        if (p.isEmpty() || p.get().getAssignedTeamId() == null) return false;
        Long teamId = p.get().getAssignedTeamId();
        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
        return members.stream().anyMatch(m ->
                m.getStudentId().equals(studentId) && "LEADER".equalsIgnoreCase(m.getRoleInTeam()));
    }

    @GetMapping("/project/{projectId}/progress")
    public Map<String, Object> progress(@PathVariable Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        int total = tasks.size();
        double avg = 0;
        long completed = 0, inProgress = 0, pending = 0;
        if (total > 0) {
            avg = tasks.stream()
                    .mapToInt(t -> t.getProgressPercent() != null ? t.getProgressPercent() : 0)
                    .average().orElse(0);
            for (Task t : tasks) {
                int p = t.getProgressPercent() != null ? t.getProgressPercent() : 0;
                if (p >= 100) completed++;
                else if (p > 0) inProgress++;
                else pending++;
            }
        }

        String assignedTo = "Not assigned";
        Optional<Project> proj = projectRepository.findById(projectId);
        if (proj.isPresent() && proj.get().getAssignedTeamId() != null) {
            assignedTo = "Team #" + proj.get().getAssignedTeamId();
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Task t : tasks) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", t.getId());
            row.put("task", t.getTitle());
            row.put("progressPercent", t.getProgressPercent() != null ? t.getProgressPercent() : 0);
            row.put("status", formatStatus(t));
            rows.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", projectId);
        result.put("assignedTo", assignedTo);
        result.put("total", total);
        result.put("completed", completed);
        result.put("inProgress", inProgress);
        result.put("pending", pending);
        result.put("percentComplete", Math.round(avg * 10) / 10.0);
        result.put("tasks", rows);
        return result;
    }

    private String formatStatus(Task t) {
        int p = t.getProgressPercent() != null ? t.getProgressPercent() : 0;
        if (p >= 100) return "Completed";
        if (p > 0) return "In Progress (" + p + "%)";
        return "Pending";
    }
}
