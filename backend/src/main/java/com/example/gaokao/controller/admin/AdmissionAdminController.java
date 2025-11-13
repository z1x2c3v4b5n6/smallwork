package com.example.gaokao.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.gaokao.domain.AdmissionStat;
import com.example.gaokao.service.AdmissionStatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/admissions")
public class AdmissionAdminController {

    private final AdmissionStatService admissionStatService;

    public AdmissionAdminController(AdmissionStatService admissionStatService) {
        this.admissionStatService = admissionStatService;
    }

    @GetMapping
    public List<AdmissionStat> list(@RequestParam(required = false) Long universityId,
                                    @RequestParam(required = false) Long majorId) {
        LambdaQueryWrapper<AdmissionStat> wrapper = new LambdaQueryWrapper<>();
        if (universityId != null) {
            wrapper.eq(AdmissionStat::getUniversityId, universityId);
        }
        if (majorId != null) {
            wrapper.eq(AdmissionStat::getMajorId, majorId);
        }
        return admissionStatService.list(wrapper);
    }

    @PutMapping("/{id}")
    public AdmissionStat update(@PathVariable Long id, @RequestBody AdmissionStat stat) {
        stat.setId(id);
        admissionStatService.updateById(stat);
        return stat;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        admissionStatService.removeById(id);
    }
}
