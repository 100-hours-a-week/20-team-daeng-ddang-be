package com.daengddang.daengdong_map.ratelimit;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RateLimitPolicy {

    public List<RateLimitRule> rules() {
        return List.of(
                rule("AUTH_LOGIN", "POST", "/api/v3/auth/login", 5, 60),
                rule("AUTH_TOKEN", "POST", "/api/v3/auth/token", 10, 60),
                rule("AUTH_LOGOUT", "POST", "/api/v3/auth/logout", 10, 60),

                rule("WALKS_START", "POST", "/api/v3/walks", 3, 60),
                rule("WALKS_END", "POST", "/api/v3/walks/{walkId}", 3, 60),
                rule("WALKS_BLOCKS", "GET", "/api/v3/walks/{walkId}/blocks", 60, 60),
                rule("WALKS_DIARY", "POST", "/api/v3/walks/{walkId}/diaries", 6, 60),

                rule("MISSIONS_ANALYSIS", "POST", "/api/v3/walks/{walkId}/missions/analysis", 5, 60),
                rule("MISSIONS_ANALYSIS_TASKS", "POST", "/api/v3/walks/{walkId}/missions/analysis-tasks", 5, 60),
                rule("MISSIONS_UPLOAD", "POST", "/api/v3/walks/{walkId}/missions", 30, 60),
                rule("MISSIONS_LIST", "GET", "/api/v3/walks/{walkId}/missions", 60, 60),

                rule("EXPRESSIONS_ANALYSIS", "POST",
                        "/api/v3/walks/{walkId}/expressions/analysis", 10, 60),
                rule("EXPRESSIONS_ANALYSIS_TASKS", "POST",
                        "/api/v3/walks/{walkId}/expressions/analysis-tasks", 10, 60),

                rule("HEALTHCARE_ANALYSIS_TASKS", "POST",
                        "/api/v3/walks/{walkId}/healthcare/analysis-tasks", 10, 60),
                rule("ANALYSIS_TASKS_GET", "GET",
                        "/api/v3/walks/{walkId}/analysis-tasks/{taskId}", 60, 60),

                rule("PRESIGNED_URL", "POST", "/api/v3/presigned-url", 30, 60),

                rule("USER_REGISTER", "POST", "/api/v3/users", 6, 60),
                rule("USER_UPDATE", "PATCH", "/api/v3/users", 6, 60),
                rule("USER_TERMS_UPDATE", "PATCH", "/api/v3/users/terms", 6, 60),
                rule("USER_WITHDRAW", "DELETE", "/api/v3/users", 2, 60),

                rule("DOG_REGISTER", "POST", "/api/v3/users/dogs", 6, 60),
                rule("DOG_UPDATE", "PATCH", "/api/v3/users/dogs", 6, 60),

                rule("BLOCKS_NEARBY", "GET", "/api/v3/blocks", 60, 60),

                rule("FOOTPRINTS_LIST", "GET", "/api/v3/footprints", 30, 60),
                rule("FOOTPRINTS_BY_DATE", "GET", "/api/v3/footprints/dates/{date}", 30, 60),
                rule("FOOTPRINT_DIARY", "GET", "/api/v3/footprints/diaries/{walkDiaryId}", 30, 60),
                rule("FOOTPRINT_DIARY_EXPRESSIONS", "GET",
                        "/api/v3/footprints/diaries/{walkDiaryId}/expressions", 30, 60)
        );
    }

    private RateLimitRule rule(String id, String method, String pattern, int limit, long windowSeconds) {
        return new RateLimitRule(id, method, pattern, limit, windowSeconds);
    }
}
