package com.example.gaokao.controller.admin;

import com.example.gaokao.domain.Major;
import com.example.gaokao.service.MajorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/majors")
public class MajorAdminController {

    private final MajorService majorService;

    public MajorAdminController(MajorService majorService) {
        this.majorService = majorService;
    }

    @GetMapping
    public List<Major> list() {
        return majorService.list();
    }

    @PostMapping
    public Major create(@RequestBody Major major) {
        majorService.save(major);
        return major;
    }

    @PutMapping("/{id}")
    public Major update(@PathVariable Long id, @RequestBody Major major) {
        major.setId(id);
        majorService.updateById(major);
        return major;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        majorService.removeById(id);
    }
}
