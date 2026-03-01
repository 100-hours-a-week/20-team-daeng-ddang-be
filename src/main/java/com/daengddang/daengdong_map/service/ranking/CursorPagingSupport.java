package com.daengddang.daengdong_map.service.ranking;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class CursorPagingSupport {

    public <VIEW, CURSOR, ITEM> CursorPageResult<ITEM> paginate(
            String cursor,
            int limit,
            Function<Integer, List<VIEW>> firstPageFetcher,
            Function<String, CURSOR> cursorParser,
            BiFunction<CURSOR, Integer, Slice<VIEW>> cursorFetcher,
            Function<VIEW, ITEM> mapper,
            Function<ITEM, String> nextCursorBuilder
    ) {
        List<ITEM> items;
        boolean hasNext;

        if (isBlank(cursor)) {
            List<VIEW> fetched = firstPageFetcher.apply(limit + 1);
            hasNext = fetched.size() > limit;
            items = fetched.stream()
                    .limit(limit)
                    .map(mapper)
                    .toList();
        } else {
            CURSOR parsedCursor = cursorParser.apply(cursor);
            Slice<VIEW> slice = cursorFetcher.apply(parsedCursor, limit);
            hasNext = slice.hasNext();
            items = slice.getContent()
                    .stream()
                    .map(mapper)
                    .toList();
        }

        String nextCursor = hasNext && !items.isEmpty()
                ? nextCursorBuilder.apply(items.get(items.size() - 1))
                : null;

        return new CursorPageResult<>(items, nextCursor, hasNext);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record CursorPageResult<T>(List<T> items, String nextCursor, boolean hasNext) {
    }
}
