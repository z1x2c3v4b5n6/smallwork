package com.example.gaokao.controller.student;

import com.example.gaokao.domain.dto.MajorSearchResult;
import com.example.gaokao.domain.dto.RecommendRequest;
import com.example.gaokao.domain.dto.RecommendResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student/recommend")
public class RecommendController {

    private final StudentSearchController searchController;

    public RecommendController(StudentSearchController searchController) {
        this.searchController = searchController;
    }

    @PostMapping
    public List<RecommendResponse> recommend(@RequestBody RecommendRequest request) {
        List<MajorSearchResult> all = searchController.search(null, null, request.getProvince(), null, null, null);
        all = all.stream().filter(item -> item.getLatestMinRank() != null).collect(Collectors.toList());
        all.sort(Comparator.comparing(MajorSearchResult::getLatestMinRank));
        List<MajorSearchResult> rush = new ArrayList<>();
        List<MajorSearchResult> match = new ArrayList<>();
        List<MajorSearchResult> safe = new ArrayList<>();
        for (MajorSearchResult item : all) {
            int diff = request.getCurrentRank() - item.getLatestMinRank();
            if (diff < -200) {
                rush.add(item);
            } else if (diff <= 200) {
                match.add(item);
            } else {
                safe.add(item);
            }
        }
        return List.of(
                new RecommendResponse("冲刺", rush.stream().limit(10).collect(Collectors.toList())),
                new RecommendResponse("稳妥", match.stream().limit(10).collect(Collectors.toList())),
                new RecommendResponse("保底", safe.stream().limit(10).collect(Collectors.toList()))
        );
    }
}
