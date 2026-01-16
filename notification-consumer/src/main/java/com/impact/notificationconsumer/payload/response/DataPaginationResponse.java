package com.impact.notificationconsumer.payload.response;

import java.util.List;

public record DataPaginationResponse<T>(long totalElements, List<T> result) {
}
