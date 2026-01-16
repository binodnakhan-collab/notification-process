package com.impact.notificationconsumer.utils;

import com.impact.notificationconsumer.payload.request.PaginationRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static com.impact.notificationconsumer.utils.Constants.*;

public class PaginationHelper {

    public static Pageable getPageable(PaginationRequest request) {
        int pageNo = Optional.of(request.getPageNo()).orElse(DEFAULT_PAGE_NO);
        int pageSize = Optional.of(request.getPageSize()).orElse(DEFAULT_PAGE_SIZE);
        String sortBy = Optional.ofNullable(request.getSortBy()).filter(s -> !s.isBlank()).orElse(DEFAULT_SORT_BY);
        Sort.Direction direction = SORT_DIRECTION.equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.ASC :
                Sort.Direction.DESC;
        return PageRequest.of(pageNo, pageSize, Sort.by(direction, sortBy));
    }
}
