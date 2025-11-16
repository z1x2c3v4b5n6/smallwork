package com.example.gaokao.controller.admin;

import com.example.gaokao.domain.dto.ImportResult;
import com.example.gaokao.service.DataImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/import")
public class ImportController {

    private final DataImportService dataImportService;

    public ImportController(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @PostMapping("/excel")
    public ResponseEntity<ImportResult> importExcel(@RequestParam("file") MultipartFile file) {
        ImportResult result = dataImportService.importExcel(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/students")
    public ResponseEntity<ImportResult> importStudents(@RequestParam("file") MultipartFile file,
                                                       @RequestParam(value = "strategy", defaultValue = "overwrite") String strategy) {
        ImportResult result = dataImportService.importStudentProfiles(file, strategy);
        return ResponseEntity.ok(result);
    }
}
