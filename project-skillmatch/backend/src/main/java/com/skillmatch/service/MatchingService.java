package com.skillmatch.service;

import com.skillmatch.model.*;
import com.skillmatch.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final ProjectRepository projectRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final UserRepository userRepository;
    private final ProjectRequiredSkillRepository projectRequiredSkillRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TaskRepository taskRepository;

    public MatchingService(ProjectRepository projectRepository,
                           StudentSkillRepository studentSkillRepository,
                           UserRepository userRepository,
                           ProjectRequiredSkillRepository projectRequiredSkillRepository,
                           TeamRepository teamRepository,
                           TeamMemberRepository teamMemberRepository,
                           TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.userRepository = userRepository;
        this.projectRequiredSkillRepository = projectRequiredSkillRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.taskRepository = taskRepository;
    }

    public List<MatchResult> recommendStudents(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        Set<Long> requiredSkillIds = new HashSet<>();
        try {
            List<ProjectRequiredSkill> required = projectRequiredSkillRepository.findByProject_Id(projectId);
            requiredSkillIds = required.stream()
                    .filter(rs -> rs.getSkill() != null)
                    .map(rs -> rs.getSkill().getId())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            System.out.println(">>> Could not load project_required_skills: " + e.getMessage());
        }

        List<User> students = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.STUDENT)
                .toList();

        List<MatchResult> results = new ArrayList<>();

        for (User student : students) {
            List<StudentSkill> studentSkills = studentSkillRepository.findByStudentId(student.getId());
            if (studentSkills == null || studentSkills.isEmpty()) continue;

            double totalScore = 0;
            int matched = 0;

            for (StudentSkill ss : studentSkills) {
                if (ss.getSkill() == null) continue;

                boolean counts = requiredSkillIds.isEmpty()
                        || requiredSkillIds.contains(ss.getSkill().getId());
                if (!counts) continue;

                matched++;
                double levelScore = switch (ss.getLevel() != null ? ss.getLevel() : StudentSkill.SkillLevel.BEGINNER) {
                    case BEGINNER -> 0.4;
                    case INTERMEDIATE -> 0.65;
                    case ADVANCED -> 0.85;
                    case EXPERT -> 1.0;
                };
                double testComponent = ss.getTestScore() != null
                        ? ss.getTestScore().doubleValue() / 100.0
                        : 0.5;
                totalScore += (levelScore * 0.6 + testComponent * 0.4);
            }

            if (matched > 0) {
                double finalScore = totalScore / matched;
                results.add(new MatchResult(
                        student.getId(),
                        student.getFullName(),
                        student.getEmail(),
                        finalScore,
                        matched));
            }
        }

        results.sort((a, b) -> Double.compare(b.score(), a.score()));
        return results;
    }

    /**
     * Skill Gap Analysis for a given project against either:
     * - A proposed/assigned team (teamId), or
     * - An individual student (studentId)
     */
    public Map<String, Object> calculateSkillGap(Long projectId, Long teamId, Long studentId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        List<ProjectRequiredSkill> requiredSkills = projectRequiredSkillRepository.findByProject_Id(projectId);

        List<User> targetStudents = new ArrayList<>();
        String targetName;
        String targetType;

        if (teamId != null) {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
            List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
            for (TeamMember m : members) {
                userRepository.findById(m.getStudentId()).ifPresent(targetStudents::add);
            }
            targetType = "TEAM";
            targetName = "Team #" + teamId + " (" + targetStudents.size() + " members)";
        } else if (studentId != null) {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
            targetStudents.add(student);
            targetType = "STUDENT";
            targetName = student.getFullName();
        } else if (project.getAssignedTeamId() != null) {
            return calculateSkillGap(projectId, project.getAssignedTeamId(), null);
        } else {
            // Default: project requirements overview
            targetType = "PROJECT_OVERVIEW";
            targetName = "Project Requirements Baseline";
        }

        List<Map<String, Object>> skillGapDetails = new ArrayList<>();
        int satisfiedCount = 0;
        int deficientCount = 0;
        int missingCount = 0;
        List<String> recommendations = new ArrayList<>();

        for (ProjectRequiredSkill req : requiredSkills) {
            if (req.getSkill() == null) continue;
            String skillName = req.getSkill().getName();
            String minLevel = req.getMinLevel() != null ? req.getMinLevel().name() : "INTERMEDIATE";
            int reqLevelRank = levelRank(minLevel);

            String bestStudentName = null;
            String bestLevel = null;
            double bestScore = 0.0;
            int highestRank = -1;

            for (User student : targetStudents) {
                List<StudentSkill> ssList = studentSkillRepository.findByStudentId(student.getId());
                for (StudentSkill ss : ssList) {
                    if (ss.getSkill() != null && skillName.equalsIgnoreCase(ss.getSkill().getName())) {
                        String lvl = ss.getLevel() != null ? ss.getLevel().name() : "BEGINNER";
                        int rank = levelRank(lvl);
                        double score = ss.getTestScore() != null ? ss.getTestScore().doubleValue() : 0.0;
                        if (rank > highestRank || (rank == highestRank && score > bestScore)) {
                            highestRank = rank;
                            bestLevel = lvl;
                            bestScore = score;
                            bestStudentName = student.getFullName();
                        }
                    }
                }
            }

            String status;
            String gapNote;
            if (highestRank == -1) {
                status = "MISSING";
                gapNote = "No member holds this skill. Critical gap.";
                missingCount++;
                recommendations.add("Critical Gap in [" + skillName + "]: Assign training or recruit a member with " + minLevel + " proficiency.");
            } else if (highestRank < reqLevelRank || bestScore < 50.0) {
                status = "DEFICIENT";
                gapNote = "Held by " + bestStudentName + " (" + bestLevel + ", Score: " + String.format("%.1f%%", bestScore) + "), but below required " + minLevel + ".";
                deficientCount++;
                recommendations.add("Improvement Needed in [" + skillName + "]: " + bestStudentName + " should take advanced practice to reach " + minLevel + ".");
            } else {
                status = "SATISFIED";
                gapNote = "Satisfied by " + bestStudentName + " (" + bestLevel + ", Score: " + String.format("%.1f%%", bestScore) + ").";
                satisfiedCount++;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("skillName", skillName);
            item.put("requiredLevel", minLevel);
            item.put("status", status);
            item.put("bestStudentName", bestStudentName != null ? bestStudentName : "None");
            item.put("bestStudentLevel", bestLevel != null ? bestLevel : "None");
            item.put("bestTestScore", bestScore);
            item.put("gapNote", gapNote);
            skillGapDetails.add(item);
        }

        int totalReq = skillGapDetails.size();
        double coveragePct = totalReq > 0
                ? ((satisfiedCount * 1.0 + deficientCount * 0.5) / totalReq) * 100.0
                : 100.0;

        String gapStatus;
        if (coveragePct >= 85.0) {
            gapStatus = "EXCELLENT_MATCH";
        } else if (coveragePct >= 65.0) {
            gapStatus = "ACCEPTABLE_MATCH";
        } else if (coveragePct >= 40.0) {
            gapStatus = "MODERATE_GAP";
        } else {
            gapStatus = "CRITICAL_GAP";
        }

        if (recommendations.isEmpty()) {
            recommendations.add("All required skills are fully satisfied with strong test scores!");
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("projectId", project.getId());
        resp.put("projectTitle", project.getTitle());
        resp.put("targetType", targetType);
        resp.put("targetName", targetName);
        resp.put("totalRequiredSkills", totalReq);
        resp.put("satisfiedCount", satisfiedCount);
        resp.put("deficientCount", deficientCount);
        resp.put("missingCount", missingCount);
        resp.put("coveragePercentage", Math.round(coveragePct * 10.0) / 10.0);
        resp.put("gapStatus", gapStatus);
        resp.put("skills", skillGapDetails);
        resp.put("recommendations", recommendations);
        return resp;
    }

    /**
     * Skill Progress & Team Performance metrics calculation
     */
    public Map<String, Object> calculateTeamPerformance(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        int totalTasks = tasks.size();
        long completed = tasks.stream().filter(t -> t.getStatus() == Task.Status.COMPLETED).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == Task.Status.IN_PROGRESS).count();
        long pending = tasks.stream().filter(t -> t.getStatus() == Task.Status.PENDING).count();

        double taskAvgPct = tasks.isEmpty() ? 0.0 :
                tasks.stream().mapToInt(t -> t.getProgressPercent() != null ? t.getProgressPercent() : 0).average().orElse(0.0);

        List<Map<String, Object>> memberStats = new ArrayList<>();
        double teamSkillAvg = 0.0;
        int memberCount = 0;
        String teamLeaderName = "Not Assigned";
        Long teamId = project.getAssignedTeamId();

        if (teamId != null) {
            List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
            memberCount = members.size();
            double totalScoreSum = 0.0;
            int scoreCount = 0;

            for (TeamMember m : members) {
                Optional<User> uOpt = userRepository.findById(m.getStudentId());
                if (uOpt.isPresent()) {
                    User u = uOpt.get();
                    if ("LEADER".equalsIgnoreCase(m.getRoleInTeam())) {
                        teamLeaderName = u.getFullName();
                    }
                    List<StudentSkill> ssList = studentSkillRepository.findByStudentId(u.getId());
                    double userScoreAvg = ssList.stream()
                            .map(StudentSkill::getTestScore)
                            .filter(Objects::nonNull)
                            .mapToDouble(BigDecimal::doubleValue)
                            .average().orElse(70.0);

                    totalScoreSum += userScoreAvg;
                    scoreCount++;

                    Map<String, Object> mInfo = new LinkedHashMap<>();
                    mInfo.put("studentId", u.getId());
                    mInfo.put("name", u.getFullName());
                    mInfo.put("role", m.getRoleInTeam());
                    mInfo.put("skillsCount", ssList.size());
                    mInfo.put("skillScoreAverage", Math.round(userScoreAvg * 10.0) / 10.0);
                    mInfo.put("status", "Active");
                    memberStats.add(mInfo);
                }
            }
            if (scoreCount > 0) {
                teamSkillAvg = totalScoreSum / scoreCount;
            }
        } else {
            teamSkillAvg = 75.0; // baseline if unassigned
        }

        // Composite Performance Index = 60% task completion + 40% team skill mastery
        double performanceIndex = (taskAvgPct * 0.6) + (teamSkillAvg * 0.4);
        String rating;
        if (performanceIndex >= 80.0) rating = "EXCELLENT";
        else if (performanceIndex >= 60.0) rating = "HIGH_PERFORMER";
        else if (performanceIndex >= 40.0) rating = "ON_TRACK";
        else rating = "NEEDS_ATTENTION";

        // Map tasks to skill progress
        List<Map<String, Object>> skillProgress = new ArrayList<>();
        for (Task t : tasks) {
            Map<String, Object> sp = new LinkedHashMap<>();
            sp.put("taskId", t.getId());
            sp.put("module", t.getTitle());
            sp.put("progressPercent", t.getProgressPercent() != null ? t.getProgressPercent() : 0);
            sp.put("status", t.getStatus() != null ? t.getStatus().name() : "PENDING");
            sp.put("associatedSkill", mapModuleToSkill(t.getTitle()));
            skillProgress.add(sp);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", project.getId());
        result.put("projectTitle", project.getTitle());
        result.put("teamId", teamId != null ? teamId : 0);
        result.put("teamLeader", teamLeaderName);
        result.put("teamSize", memberCount);
        result.put("overallProgressPercent", Math.round(taskAvgPct * 10.0) / 10.0);
        result.put("teamSkillAverage", Math.round(teamSkillAvg * 10.0) / 10.0);
        result.put("performanceScore", Math.round(performanceIndex * 10.0) / 10.0);
        result.put("performanceRating", rating);
        result.put("tasksCompleted", completed);
        result.put("tasksInProgress", inProgress);
        result.put("tasksPending", pending);
        result.put("totalTasks", totalTasks);
        result.put("skillProgress", skillProgress);
        result.put("members", memberStats);
        return result;
    }

    /**
     * Matches open projects for a specific student
     */
    public List<Map<String, Object>> matchProjectsForStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        List<StudentSkill> studentSkills = studentSkillRepository.findByStudentId(studentId);
        Map<String, StudentSkill> studentSkillMap = new HashMap<>();
        for (StudentSkill ss : studentSkills) {
            if (ss.getSkill() != null) {
                studentSkillMap.put(ss.getSkill().getName().toLowerCase(), ss);
            }
        }

        List<Project> openProjects = projectRepository.findAll().stream()
                .filter(p -> p.getStatus() == Project.Status.OPEN)
                .toList();

        List<Map<String, Object>> list = new ArrayList<>();
        for (Project p : openProjects) {
            List<ProjectRequiredSkill> reqList = projectRequiredSkillRepository.findByProject_Id(p.getId());
            int totalReq = reqList.size();
            int matched = 0;
            double scoreSum = 0.0;
            List<String> matchedSkillNames = new ArrayList<>();
            List<String> missingSkillNames = new ArrayList<>();

            for (ProjectRequiredSkill prs : reqList) {
                if (prs.getSkill() == null) continue;
                String name = prs.getSkill().getName();
                StudentSkill ss = studentSkillMap.get(name.toLowerCase());
                if (ss != null) {
                    matched++;
                    matchedSkillNames.add(name);
                    double score = ss.getTestScore() != null ? ss.getTestScore().doubleValue() : 50.0;
                    scoreSum += score;
                } else {
                    missingSkillNames.add(name);
                }
            }

            double matchPct;
            if (totalReq > 0) {
                matchPct = (matched * 100.0) / totalReq;
            } else {
                matchPct = studentSkills.isEmpty() ? 50.0 : 80.0;
            }

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("projectId", p.getId());
            map.put("title", p.getTitle());
            map.put("teamSize", p.getTeamSize());
            map.put("status", p.getStatus().name());
            map.put("totalRequiredSkills", totalReq);
            map.put("matchedSkillsCount", matched);
            map.put("matchPercentage", Math.round(matchPct * 10.0) / 10.0);
            map.put("matchedSkills", matchedSkillNames);
            map.put("missingSkills", missingSkillNames);
            list.add(map);
        }

        list.sort((a, b) -> Double.compare(
                (Double) b.get("matchPercentage"),
                (Double) a.get("matchPercentage")
        ));
        return list;
    }

    private int levelRank(String level) {
        if (level == null) return 1;
        return switch (level.toUpperCase()) {
            case "BEGINNER" -> 1;
            case "INTERMEDIATE" -> 2;
            case "ADVANCED" -> 3;
            case "EXPERT" -> 4;
            default -> 1;
        };
    }

    private String mapModuleToSkill(String moduleTitle) {
        String lower = moduleTitle != null ? moduleTitle.toLowerCase() : "";
        if (lower.contains("front") || lower.contains("ui") || lower.contains("web")) return "React / Frontend";
        if (lower.contains("back") || lower.contains("api") || lower.contains("service")) return "Java / Spring Boot";
        if (lower.contains("data") || lower.contains("sql") || lower.contains("storage")) return "MySQL / Database";
        if (lower.contains("test") || lower.contains("qa")) return "JUnit / Testing";
        if (lower.contains("report") || lower.contains("doc")) return "Documentation & PM";
        return "Software Engineering";
    }

    public record MatchResult(Long studentId, String fullName, String email,
                              double score, int matchedSkills) {}
}
