package com.ssatr.Project.repository;

import com.ssatr.Project.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, String> {
    List<Survey> findAllByUserIdOrderByFeedbackStartDesc(String userId);
}
