package com.example.gaokao.service;

import com.example.gaokao.controller.student.StudentSearchController;
import com.example.gaokao.domain.dto.MajorSearchResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentSearchControllerAdapter {

    private final StudentSearchController studentSearchController;

    public StudentSearchControllerAdapter(StudentSearchController studentSearchController) {
        this.studentSearchController = studentSearchController;
    }

    public List<MajorSearchResult> searchForRecommendation(String province) {
        return studentSearchController.search(null, null, province, null, null, null);
    }
}
