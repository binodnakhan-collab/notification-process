package com.impact.notificationconsumer.payload.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginationRequest {

    private int pageNo;
    private int pageSize;
    private String sortBy;
    private String sortDirection;
}
