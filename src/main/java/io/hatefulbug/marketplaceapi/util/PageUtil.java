package io.hatefulbug.marketplaceapi.util;

import io.hatefulbug.marketplaceapi.request.PageResponse;
import org.springframework.data.domain.Page;

public class PageUtil {

    public static <T> PageResponse<T> getPage(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}

