package com.skillmatch.repository;

import com.skillmatch.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByFacultyId(Long facultyId);
    List<Project> findByStatus(Project.Status status);
}
