package com.skillmatch.repository;

import com.skillmatch.model.ProjectRequiredSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRequiredSkillRepository extends JpaRepository<ProjectRequiredSkill, Long> {
    List<ProjectRequiredSkill> findByProject_Id(Long projectId);
}
