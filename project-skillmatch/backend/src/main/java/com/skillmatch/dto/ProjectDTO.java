package com.skillmatch.dto;

import com.skillmatch.model.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {
    private Long id;
    private Long facultyId;
    private String title;
    private String description;
    private Integer teamSize;
    private String status;
    private Long assignedTeamId;
    private String assignedTo;  // human-readable: "Team #3 (Leader: Aarav Sharma)"
    private LocalDateTime createdAt;
    private List<String> requiredSkills;

    public static ProjectDTO from(Project p) {
        return from(p, null, null);
    }

    public static ProjectDTO from(Project p, String assignedToLabel) {
        return from(p, assignedToLabel, null);
    }

    public static ProjectDTO from(Project p, String assignedToLabel, List<String> requiredSkills) {
        return ProjectDTO.builder()
                .id(p.getId())
                .facultyId(p.getFacultyId())
                .title(p.getTitle())
                .description(p.getDescription())
                .teamSize(p.getTeamSize())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .assignedTeamId(p.getAssignedTeamId())
                .assignedTo(assignedToLabel != null ? assignedToLabel
                        : (p.getAssignedTeamId() != null ? "Team #" + p.getAssignedTeamId() : "Not assigned"))
                .createdAt(p.getCreatedAt())
                .requiredSkills(requiredSkills)
                .build();
    }
}
