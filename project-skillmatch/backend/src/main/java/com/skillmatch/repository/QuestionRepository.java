package com.skillmatch.repository;

import com.skillmatch.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByCourseIgnoreCase(String course);

    @Query("SELECT DISTINCT q.course FROM Question q ORDER BY q.course ASC")
    List<String> findDistinctCourses();

    long countByCourseIgnoreCase(String course);
}

