package com.example.gaokao.controller.student;

import com.example.gaokao.domain.AdmissionStat;
import com.example.gaokao.domain.Major;
import com.example.gaokao.domain.University;
import com.example.gaokao.domain.UniversityMajor;
import com.example.gaokao.domain.dto.MajorSearchResult;
import com.example.gaokao.service.AdmissionStatService;
import com.example.gaokao.service.MajorService;
import com.example.gaokao.service.UniversityMajorService;
import com.example.gaokao.service.UniversityService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
public class StudentSearchController {

    private final UniversityService universityService;
    private final MajorService majorService;
    private final UniversityMajorService universityMajorService;
    private final AdmissionStatService admissionStatService;

    public StudentSearchController(UniversityService universityService, MajorService majorService,
                                   UniversityMajorService universityMajorService, AdmissionStatService admissionStatService) {
        this.universityService = universityService;
        this.majorService = majorService;
        this.universityMajorService = universityMajorService;
        this.admissionStatService = admissionStatService;
    }

    @GetMapping("/search")
    public List<MajorSearchResult> search(@RequestParam(required = false) String universityName,
                                          @RequestParam(required = false) String majorName,
                                          @RequestParam(required = false) String province,
                                          @RequestParam(required = false) String batch,
                                          @RequestParam(required = false) String category,
                                          @RequestParam(required = false) String level) {
        List<University> universities = universityService.list();
        Map<Long, University> universityMap = universities.stream()
                .filter(u -> universityName == null || u.getName().contains(universityName))
                .filter(u -> province == null || province.isEmpty() || province.equals(u.getProvince()))
                .collect(Collectors.toMap(University::getId, u -> u, (a, b) -> a));

        List<Major> majors = majorService.list();
        Map<Long, Major> majorMap = majors.stream()
                .filter(m -> majorName == null || m.getName().contains(majorName))
                .filter(m -> category == null || category.isEmpty() || category.equals(m.getCategory()))
                .filter(m -> level == null || level.isEmpty() || level.equals(m.getLevel()))
                .collect(Collectors.toMap(Major::getId, m -> m, (a, b) -> a));

        List<UniversityMajor> universityMajors = universityMajorService.list();
        List<MajorSearchResult> results = new ArrayList<>();
        for (UniversityMajor um : universityMajors) {
            University university = universityMap.get(um.getUniversityId());
            Major major = majorMap.get(um.getMajorId());
            if (university == null || major == null) {
                continue;
            }
            if (batch != null && !batch.isEmpty() && !batch.equals(um.getBatch())) {
                continue;
            }
            AdmissionStat latest = admissionStatService.lambdaQuery()
                    .eq(AdmissionStat::getUniversityId, um.getUniversityId())
                    .eq(AdmissionStat::getMajorId, um.getMajorId())
                    .orderByDesc(AdmissionStat::getYear)
                    .last("limit 1")
                    .one();
            results.add(new MajorSearchResult(
                    university.getId(),
                    university.getName(),
                    university.getProvince(),
                    major.getId(),
                    major.getName(),
                    major.getCategory(),
                    major.getSubjectReq(),
                    major.getLevel(),
                    um.getBatch(),
                    latest != null ? latest.getMinScore() : null,
                    latest != null ? latest.getMinRank() : null
            ));
        }
        results.sort(Comparator.comparing(MajorSearchResult::getLatestMinRank, Comparator.nullsLast(Integer::compareTo)));
        return results;
    }

    @GetMapping("/universities/{universityId}/majors/{majorId}")
    public List<AdmissionStat> detail(@PathVariable Long universityId, @PathVariable Long majorId) {
        return admissionStatService.lambdaQuery()
                .eq(AdmissionStat::getUniversityId, universityId)
                .eq(AdmissionStat::getMajorId, majorId)
                .orderByDesc(AdmissionStat::getYear)
                .list();
    }
}
