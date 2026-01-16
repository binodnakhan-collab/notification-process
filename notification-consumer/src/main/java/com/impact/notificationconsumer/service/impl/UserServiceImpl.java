package com.impact.notificationconsumer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.impact.notificationconsumer.entity.UserEntity;
import com.impact.notificationconsumer.exception.CustomException;
import com.impact.notificationconsumer.payload.request.PaginationRequest;
import com.impact.notificationconsumer.payload.response.DataPaginationResponse;
import com.impact.notificationconsumer.payload.response.GlobalResponse;
import com.impact.notificationconsumer.payload.response.UserResponse;
import com.impact.notificationconsumer.repository.UserRepository;
import com.impact.notificationconsumer.service.UserService;
import com.impact.notificationconsumer.utils.PaginationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public GlobalResponse getAllUsers(PaginationRequest paginationRequest) {
        Pageable pageable = PaginationHelper.getPageable(paginationRequest);
        Page<UserEntity> userPage = userRepository.findAll(pageable);
        List<UserResponse> userListResponse = userPage.getContent().stream().map(this::userResponseBuilder).toList();
        DataPaginationResponse<UserResponse> finalResponse = new DataPaginationResponse<>(userPage.getNumberOfElements(), userListResponse);
        /*
         * Here we create generic response which contain status (success true or false), message and data as object.
         * instead of returning raw user response without any message or status in response body I create a generic response for simplify.
         * */
        return new GlobalResponse("User list fetch success.", finalResponse);
    }

    @Override
    public GlobalResponse getUserById(Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new CustomException("User not found with id: %s", HttpStatus.NOT_FOUND, userId));
        UserResponse userResponse = userResponseBuilder(user);
        return new GlobalResponse("User detail fetch success.", userResponse);
    }

    /*
     * here we can use modelMapper or MapStruct to map entity to dto. for now, we use builder instead adding additional dependencies
     * */
    private UserResponse userResponseBuilder(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .address(user.getAddress())
                .doe(user.getDoe())
                .country(user.getCountry())
                .notificationEventContext(user.getNotificationEventContext())
                .build();
    }
}
