package com.ssatr.Project.repository;

import com.ssatr.Project.entity.Response;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResponseRepository extends JpaRepository<Response, String> {
    List<Response> findAllByQuestionIdIn(List<String> questionIds);
}
