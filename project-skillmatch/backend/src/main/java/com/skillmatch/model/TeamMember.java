package com.skillmatch.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "team_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Builder.Default
    @Column(name = "role_in_team", length = 60)
    private String roleInTeam = "MEMBER";
}
