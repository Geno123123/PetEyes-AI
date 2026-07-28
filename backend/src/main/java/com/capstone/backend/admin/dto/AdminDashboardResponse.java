package com.capstone.backend.admin.dto;

import java.util.List;

public record AdminDashboardResponse(
        StatsResponse summary,
        List<DailyMetric> dailyMetrics,
        List<CategoryShare> diagnosisCategoryShares,
        List<CategoryShare> qnaSpeciesShares
) {
    public record DailyMetric(
            String date,
            long userSignups,
            long cumulativeUsers,
            long diagnoses,
            long reviews,
            long qnaPosts
    ) {}

    public record CategoryShare(
            String category,
            long count
    ) {}
}
