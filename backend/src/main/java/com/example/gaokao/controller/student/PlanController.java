package com.example.gaokao.controller.student;

import com.example.gaokao.domain.Plan;
import com.example.gaokao.domain.PlanItem;
import com.example.gaokao.domain.User;
import com.example.gaokao.domain.dto.PlanItemRequest;
import com.example.gaokao.service.PlanItemService;
import com.example.gaokao.service.PlanService;
import com.example.gaokao.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/student/plans")
public class PlanController {

    private final PlanService planService;
    private final PlanItemService planItemService;
    private final UserService userService;

    public PlanController(PlanService planService, PlanItemService planItemService, UserService userService) {
        this.planService = planService;
        this.planItemService = planItemService;
        this.userService = userService;
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof User user) {
            username = user.getUsername();
        } else {
            username = principal.toString();
        }
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getId();
    }

    @GetMapping
    public List<Plan> list(Authentication authentication) {
        Long userId = getUserId(authentication);
        return planService.lambdaQuery()
                .eq(Plan::getUserId, userId)
                .orderByDesc(Plan::getCreateTime)
                .list();
    }

    @PostMapping
    public Plan create(@RequestBody Plan plan, Authentication authentication) {
        Long userId = getUserId(authentication);
        plan.setUserId(userId);
        plan.setCreateTime(LocalDateTime.now());
        planService.save(plan);
        return plan;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication authentication) {
        Long userId = getUserId(authentication);
        planService.lambdaUpdate().eq(Plan::getId, id).eq(Plan::getUserId, userId).remove();
        planItemService.lambdaUpdate().eq(PlanItem::getPlanId, id).remove();
    }

    @GetMapping("/{planId}/items")
    public List<PlanItem> listItems(@PathVariable Long planId, Authentication authentication) {
        Long userId = getUserId(authentication);
        Plan plan = planService.getById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            return List.of();
        }
        return planItemService.lambdaQuery().eq(PlanItem::getPlanId, planId).list();
    }

    @PostMapping("/{planId}/items")
    public PlanItem addItem(@PathVariable Long planId, @RequestBody PlanItemRequest request, Authentication authentication) {
        Long userId = getUserId(authentication);
        Plan plan = planService.getById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("无权限");
        }
        PlanItem item = new PlanItem();
        item.setPlanId(planId);
        item.setUniversityId(request.getUniversityId());
        item.setMajorId(request.getMajorId());
        item.setBatch(request.getBatch());
        item.setOrderNo(request.getOrderNo());
        planItemService.save(item);
        return item;
    }

    @DeleteMapping("/{planId}/items/{itemId}")
    public void deleteItem(@PathVariable Long planId, @PathVariable Long itemId, Authentication authentication) {
        Long userId = getUserId(authentication);
        Plan plan = planService.getById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            return;
        }
        planItemService.lambdaUpdate().eq(PlanItem::getId, itemId).eq(PlanItem::getPlanId, planId).remove();
    }
}
