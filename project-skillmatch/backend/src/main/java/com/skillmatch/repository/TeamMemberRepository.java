package com.skillmatch.repository;

import com.skillmatch.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByTeamId(Long teamId);
    List<TeamMember> findByStudentId(Long studentId);

    @Transactional
    void deleteByStudentId(Long studentId);

    @Transactional
    void deleteByTeamId(Long teamId);
}
