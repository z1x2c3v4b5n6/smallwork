package com.example.gaokao.controller.student;

import com.example.gaokao.domain.*;
import com.example.gaokao.domain.dto.PlanItemRequest;
import com.example.gaokao.domain.dto.PlanItemView;
import com.example.gaokao.service.*;
import com.example.gaokao.util.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student/plans")
public class PlanController {

    private final PlanService planService;
    private final PlanItemService planItemService;
    private final UserService userService;
    private final StudentProfileService studentProfileService;
    private final UniversityService universityService;
    private final MajorService majorService;
    private final AdmissionStatService admissionStatService;
    private final RecommendationEngine recommendationEngine;

    public PlanController(PlanService planService, PlanItemService planItemService, UserService userService,
                          StudentProfileService studentProfileService, UniversityService universityService,
                          MajorService majorService, AdmissionStatService admissionStatService,
                          RecommendationEngine recommendationEngine) {
        this.planService = planService;
        this.planItemService = planItemService;
        this.userService = userService;
        this.studentProfileService = studentProfileService;
        this.universityService = universityService;
        this.majorService = majorService;
        this.admissionStatService = admissionStatService;
        this.recommendationEngine = recommendationEngine;
    }

    @GetMapping
    public List<Plan> list(Authentication authentication) {
        Long userId = SecurityUtils.getUserId(authentication, userService);
        return planService.lambdaQuery()
                .eq(Plan::getUserId, userId)
                .orderByDesc(Plan::getCreateTime)
                .list();
    }

    @PostMapping
    public Plan create(@RequestBody Plan plan, Authentication authentication) {
        Long userId = SecurityUtils.getUserId(authentication, userService);
        plan.setUserId(userId);
        plan.setCreateTime(LocalDateTime.now());
        planService.save(plan);
        return plan;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication authentication) {
        Long userId = SecurityUtils.getUserId(authentication, userService);
        planService.lambdaUpdate().eq(Plan::getId, id).eq(Plan::getUserId, userId).remove();
        planItemService.lambdaUpdate().eq(PlanItem::getPlanId, id).remove();
    }

    @GetMapping("/{planId}/items")
    public List<PlanItemView> listItems(@PathVariable Long planId, Authentication authentication) {
        Long userId = SecurityUtils.getUserId(authentication, userService);
        Plan plan = planService.getById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            return List.of();
        }
        List<PlanItem> items = planItemService.lambdaQuery()
                .eq(PlanItem::getPlanId, planId)
                .orderByAsc(PlanItem::getOrderNo)
                .list();
        if (items.isEmpty()) {
            return List.of();
        }
        List<Long> universityIds = items.stream().map(PlanItem::getUniversityId).distinct()
                .collect(Collectors.toList());
        List<Long> majorIds = items.stream().map(PlanItem::getMajorId).distinct()
                .collect(Collectors.toList());
        Map<Long, University> universityMap = universityService.listByIds(universityIds).stream()
                .collect(Collectors.toMap(University::getId, u -> u, (a, b) -> a));
        Map<Long, Major> majorMap = majorService.listByIds(majorIds).stream()
                .collect(Collectors.toMap(Major::getId, m -> m, (a, b) -> a));
        StudentProfile profile = studentProfileService.lambdaQuery().eq(StudentProfile::getUserId, userId).one();
        Integer currentRank = profile != null ? profile.getRank() : null;
        return items.stream().map(item -> {
            University university = universityMap.get(item.getUniversityId());
            Major major = majorMap.get(item.getMajorId());
            AdmissionStat latest = admissionStatService.lambdaQuery()
                    .eq(AdmissionStat::getUniversityId, item.getUniversityId())
                    .eq(AdmissionStat::getMajorId, item.getMajorId())
                    .orderByDesc(AdmissionStat::getYear)
                    .last("limit 1")
                    .one();
            RecommendationEngine.RiskLevel riskLevel = recommendationEngine.evaluateRisk(currentRank,
                    latest != null ? latest.getMinRank() : null);
            return PlanItemView.builder()
                    .id(item.getId())
                    .orderNo(item.getOrderNo())
                    .batch(item.getBatch())
                    .universityId(item.getUniversityId())
                    .universityName(university != null ? university.getName() : "--")
                    .majorId(item.getMajorId())
                    .majorName(major != null ? major.getName() : "--")
                    .latestMinRank(latest != null ? latest.getMinRank() : null)
                    .latestMinScore(latest != null ? latest.getMinScore() : null)
                    .riskLabel(riskLevel.getLabel())
                    .riskType(riskLevel.getTagType())
                    .build();
        }).collect(Collectors.toList());
    }

    @PostMapping("/{planId}/items")
    public PlanItem addItem(@PathVariable Long planId, @RequestBody PlanItemRequest request, Authentication authentication) {
        Long userId = SecurityUtils.getUserId(authentication, userService);
        Plan plan = planService.getById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("无权限");
        }
        PlanItem item = new PlanItem();
        item.setPlanId(planId);
        item.setUniversityId(request.getUniversityId());
        item.setMajorId(request.getMajorId());
        item.setBatch(request.getBatch());
        Integer orderNo = request.getOrderNo();
        if (orderNo == null) {
            long count = planItemService.lambdaQuery().eq(PlanItem::getPlanId, planId).count();
            orderNo = (int) count + 1;
        }
        item.setOrderNo(orderNo);
        planItemService.save(item);
        return item;
    }

    @DeleteMapping("/{planId}/items/{itemId}")
    public void deleteItem(@PathVariable Long planId, @PathVariable Long itemId, Authentication authentication) {
        Long userId = SecurityUtils.getUserId(authentication, userService);
        Plan plan = planService.getById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            return;
        }
        planItemService.lambdaUpdate().eq(PlanItem::getId, itemId).eq(PlanItem::getPlanId, planId).remove();
    }
}
