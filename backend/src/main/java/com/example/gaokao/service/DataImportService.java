package com.example.gaokao.service;

import org.springframework.web.multipart.MultipartFile;

public interface DataImportService {
    int importExcel(MultipartFile file);
    int importStudentProfiles(MultipartFile file, String strategy);
}
