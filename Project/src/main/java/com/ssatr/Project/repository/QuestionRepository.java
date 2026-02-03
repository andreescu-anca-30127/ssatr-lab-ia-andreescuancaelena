package com.ssatr.Project.repository;

import com.ssatr.Project.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, String> {
    List<Question> findAllBySurveyId(String surveyId);
}
