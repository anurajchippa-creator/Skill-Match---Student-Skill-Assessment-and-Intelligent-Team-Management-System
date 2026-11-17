package com.skillmatch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "faculty_id", nullable = false)
    private Long facultyId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "team_size", nullable = false)
    private Integer teamSize = 3;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.OPEN;

    /** Set when faculty approves a team – one project, one assigned team */
    @Column(name = "assigned_team_id")
    private Long assignedTeamId;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProjectRequiredSkill> requiredSkills = new HashSet<>();

    public enum Status {
        OPEN, MATCHING, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
