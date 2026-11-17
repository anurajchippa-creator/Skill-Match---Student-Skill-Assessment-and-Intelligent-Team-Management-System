package com.skillmatch.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.PROPOSED;

    @Column(name = "created_by")
    private Long createdBy; // student who initiated

    @Column(length = 500)
    private String note;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        PROPOSED, APPROVED, REJECTED, ACTIVE, COMPLETED
    }
}
