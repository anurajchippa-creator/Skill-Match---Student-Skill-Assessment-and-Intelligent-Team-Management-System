package com.skillmatch.repository;

import com.skillmatch.model.StudentSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface StudentSkillRepository extends JpaRepository<StudentSkill, Long> {
    List<StudentSkill> findByStudentId(Long studentId);

    @Transactional
    void deleteByStudentId(Long studentId);
}
