package com.example.gaokao.controller.admin;

import com.example.gaokao.service.DataImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/import")
public class ImportController {

    private final DataImportService dataImportService;

    public ImportController(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @PostMapping("/excel")
    public ResponseEntity<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        int count = dataImportService.importExcel(file);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", count);
        return ResponseEntity.ok(result);
    }
}
