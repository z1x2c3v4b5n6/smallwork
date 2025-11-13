package com.example.gaokao.controller.admin;

import com.example.gaokao.domain.University;
import com.example.gaokao.service.UniversityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/universities")
public class UniversityAdminController {

    private final UniversityService universityService;

    public UniversityAdminController(UniversityService universityService) {
        this.universityService = universityService;
    }

    @GetMapping
    public List<University> list() {
        return universityService.list();
    }

    @PostMapping
    public University create(@RequestBody University university) {
        universityService.save(university);
        return university;
    }

    @PutMapping("/{id}")
    public University update(@PathVariable Long id, @RequestBody University university) {
        university.setId(id);
        universityService.updateById(university);
        return university;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        universityService.removeById(id);
    }
}
