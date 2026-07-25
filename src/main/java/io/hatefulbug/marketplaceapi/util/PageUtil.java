package io.hatefulbug.marketplaceapi.util;

import org.springframework.data.domain.Page;

import io.hatefulbug.marketplaceapi.request.PageResponse;

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

