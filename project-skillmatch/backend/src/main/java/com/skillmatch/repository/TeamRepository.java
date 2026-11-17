package com.skillmatch.repository;

import com.skillmatch.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByProjectId(Long projectId);
    List<Team> findByStatus(Team.Status status);
}
