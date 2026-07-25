package io.hatefulbug.marketplaceapi.request;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public PageResponse {
        content = content == null
                ? List.of()
                : List.copyOf(content);
    }
}
