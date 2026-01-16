package com.impact.notificationconsumer.service;

import com.impact.notificationconsumer.payload.request.PaginationRequest;
import com.impact.notificationconsumer.payload.response.GlobalResponse;
import com.impact.notificationconsumer.payload.response.UserResponse;

public interface UserService {

    GlobalResponse getAllUsers(PaginationRequest paginationRequest);
    GlobalResponse getUserById(Long userId);
}
