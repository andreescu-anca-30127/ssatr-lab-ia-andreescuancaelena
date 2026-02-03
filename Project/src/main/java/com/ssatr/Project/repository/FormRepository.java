package com.ssatr.Project.repository;

import com.ssatr.Project.entity.Form;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormRepository extends JpaRepository<Form, String> {
    List<Form> findAllBySurvey(String surveyId);
}
