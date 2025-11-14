package com.example.gaokao.controller.admin;

import com.example.gaokao.domain.UniversityMajor;
import com.example.gaokao.service.UniversityMajorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/university-majors")
public class UniversityMajorAdminController {

    private final UniversityMajorService universityMajorService;

    public UniversityMajorAdminController(UniversityMajorService universityMajorService) {
        this.universityMajorService = universityMajorService;
    }

    @GetMapping
    public List<UniversityMajor> list() {
        return universityMajorService.list();
    }

    @PostMapping
    public UniversityMajor create(@RequestBody UniversityMajor universityMajor) {
        universityMajorService.save(universityMajor);
        return universityMajor;
    }

    @PutMapping("/{id}")
    public UniversityMajor update(@PathVariable Long id, @RequestBody UniversityMajor universityMajor) {
        universityMajor.setId(id);
        universityMajorService.updateById(universityMajor);
        return universityMajor;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        universityMajorService.removeById(id);
    }
}
