package com.skillmatch.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Work module for a project (Frontend, Backend, Database, Testing, Reporting, ...).
 * Team leader updates progressPercent (0-100).
 */
@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Builder.Default
    @Column(name = "progress_percent")
    private Integer progressPercent = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Column(name = "due_date")
    private LocalDate dueDate;

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED
    }

    /** Keep status in sync with percent */
    public void syncStatusFromPercent() {
        int p = progressPercent != null ? progressPercent : 0;
        if (p >= 100) status = Status.COMPLETED;
        else if (p > 0) status = Status.IN_PROGRESS;
        else status = Status.PENDING;
    }
}
