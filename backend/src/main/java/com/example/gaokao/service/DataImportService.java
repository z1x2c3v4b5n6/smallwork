package com.example.gaokao.service;

import com.example.gaokao.domain.dto.ImportResult;
import org.springframework.web.multipart.MultipartFile;

public interface DataImportService {
    ImportResult importExcel(MultipartFile file);
    ImportResult importStudentProfiles(MultipartFile file, String strategy);
}
